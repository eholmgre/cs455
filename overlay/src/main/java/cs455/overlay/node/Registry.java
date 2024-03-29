package cs455.overlay.node;

import cs455.overlay.events.*;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.util.Overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Registry implements Node {

    protected class RegistryHelperThread implements Runnable {

        @Override
        public void run() {
            System.out.println("Registry helper thread starting.");
            while (getState() != RegistryState.EXITING) {
                try {
                    Event e = eventQueue.take();

                    switch (e.getType()) {
                        case REGISTER_REQUEST:
                            handleRegisterRequest((RegisterRequest) e);
                            break;
                        case DEREGISTER_REQUEST:
                            handleDeregisterRequest((DeregisterRequest) e);
                            break;
                        case TASK_COMPLETE:
                            handleTaskComplete((TaskComplete) e);
                            break;
                        case TRAFFIC_SUMMARY:
                            handleTrafficSummary((TrafficSummary) e);
                    }
                } catch (InterruptedException e) {
                    System.err.println("Helper thread interrupted" + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.err.println("Error: IOException in helper thread.");
                }

            }
            System.out.println("Registry helper thread stopping.");

        }
    }

    private enum RegistryState {
        REGISTRATION, CREATE_OVERLAY, SEND_WEIGHTS, TASK_STARTED, PULL_TRAFFIC, PRINT_STATS, EXITING
    }

    private String[] progArgs;

    private RegistryState registryState;

    private String myIP;
    private int myPort;

    private Overlay overlay;

    private TCPServerThread tcpServer;
    private Thread serverThread;

    private RegistryHelperThread helper;
    private Thread helperThread;

    private ConnectionManager connectionManager;

    private LinkedBlockingQueue<Event> eventQueue;

    public Registry(String[] args) {
        progArgs = args;
        registryState = RegistryState.REGISTRATION;
        connectionManager = new ConnectionManager(this);
        eventQueue = new LinkedBlockingQueue<>();
        overlay = new Overlay();
    }

    private synchronized RegistryState getState() {
        return registryState;
    }

    private synchronized void setState(RegistryState state) {
        registryState = state;
    }

    private void handleTrafficSummary(TrafficSummary message) throws IOException {
        overlay.setSummary(message.getIp() + ":" + message.getPort(), message.getNumSent(), message.getNumRcvd(),
                message.getNumRlyd(), message.getTotalSent(), message.getTotalRcvd());

        if (overlay.allSummaries()) {
            overlay.printSummaries();
            overlay.resetSummaries();

            setState(RegistryState.SEND_WEIGHTS);
        }
    }

    private void handleTaskComplete(TaskComplete message) throws IOException {
        String nodeId = message.getIp() + ":" + message.getPort();
        overlay.setComplete(nodeId);
        System.out.println("Received task-complete from " + nodeId);

        if (overlay.allComplete()) {
            try {
                System.out.println("All nodes task complete, sleeping for 15 seconds to let all messages reach their destinations");
                Thread.sleep(15000);
                System.out.println("sending pull summaries");

                connectionManager.broadcast(new PullTrafficSummaries("localhost", -1));

            } catch (InterruptedException e) {
                System.out.println("interupted while waiting to send task complete.");
            }
        }
    }

    private void handleRegisterRequest(RegisterRequest e) throws IOException {
        boolean success = true;
        String info = "";

        String requestId = e.getIp() + ':' + e.getPort();

        int connectioID = e.getConnectionId();

        if (getState() != RegistryState.REGISTRATION) {
            success = false;
            info = "Registration period is over. ";
        }

        if (!e.getIp().equals(e.getOrigin())) {
            // Error: request origin and ip do not match
            success = false;
            info += "Request ip field does not match request origin. ";
        }

        if (overlay.inNodes(requestId)) {
            success = false;
            info += "Node has already been registered. ";
        }

        if (success) {
            info = "Welcome aboard";
            overlay.addNode(e.getIp(), e.getPort(), connectioID);
        }

        int registerCount = overlay.getCount();

        RegisterResponse response = new RegisterResponse(success, info, registerCount, "localhost", connectioID);

        connectionManager.sendMessage(connectioID, response);

        System.out.println("Registration " + (success ? "successful" : "failed") + " for node " + requestId
                + " (" + info + "). " + registerCount + " nodes registered.");
    }

    private void handleDeregisterRequest(DeregisterRequest e) throws IOException, InterruptedException {
        boolean success = true;
        String info = "";

        String requestId = e.getIp() + ":" + e.getPort();

        int connectionId = e.getConnectionId();

        if (!overlay.inNodes(requestId)) {
            success = false;
        }

        if (!e.getIp().equals(e.getOrigin())) {
            // Error: request origin and ip do not match
            success = false;
            info = "Request ip field does not match request origin";
        }

        if (success) {
            info = "well, bye";

            overlay.removeNode(requestId);
        }

        int registerCount = overlay.getCount();

        DeregisterResponse response = new DeregisterResponse(success, registerCount, info, "localhost", connectionId);

        connectionManager.sendMessage(connectionId, response);

        connectionManager.closeConnection(connectionId);

        System.out.println("Deregistration " + (success ? "successful" : "failed") + " for node " + requestId
                + " (" + info + "). " + registerCount + " nodes registered.");
    }

    private void setupOverlay(int connectionRequirement) throws IOException {
        if (overlay.getCount() < 2) {
            System.out.println("Not enough nodes have registered yet.");
            return;
        }
        ArrayList<String[]> connections = overlay.generateOverlay(connectionRequirement);

        /*
        if (connections == null) {
            System.out.println("");
            return;
        } */

        HashMap<String, String> nodeLists = new HashMap<>();
        HashMap<String, Integer> nodeNums = new HashMap<>();

        for (String[] con : connections) {
            if (nodeLists.containsKey(con[0])) {
                String curList = nodeLists.get(con[0]);
                int curCount = nodeNums.get(con[0]);
                curList += "," + con[1];
                nodeLists.replace(con[0], curList);
                nodeNums.replace(con[0], ++curCount);
            } else {
                nodeLists.put(con[0], con[1]);
                nodeNums.put(con[0], 1);
            }
        }

        for (String[] node : overlay.getNodes()) {
            String nodeId = node[0] + ":" + node[1];
            int connectionId = overlay.getConnectionId(nodeId);

            connectionManager.sendMessage(connectionId, new MessagingNodesList(nodeNums.get(nodeId), nodeLists.get(nodeId), "localhost", -1));
        }

        setState(RegistryState.CREATE_OVERLAY);

        System.out.println("Setup overlay with connection requirement " + connectionRequirement);
    }

    private void sendWeights() throws IOException {
        overlay.generateWeights();

        ArrayList<String[]> weights = overlay.getConnectionWeights();

        connectionManager.broadcast(new LinkWeights(weights.size(), weights, "localhost", -1));

        setState(RegistryState.SEND_WEIGHTS);
        System.out.println("Sent connection weights to overlay.");
    }

    private void taskStart(int numRounds) throws IOException{
        connectionManager.broadcast(new TaskInitiate(numRounds, "localhost", -1));
        System.out.println("Starting " + numRounds + " rounds in overlay.");
        setState(RegistryState.TASK_STARTED);
    }


    @Override
    public void onEvent(Event event) {
        eventQueue.offer(event);

    }

    private void printUsage() {
        System.out.println("Usage: registry <tcp port>");
    }

    public void startRegistry() {
        if (progArgs.length != 1) {
            printUsage();
            System.exit(1);
        }

        try {
            myIP = InetAddress.getLocalHost().getHostAddress();
            myPort = Integer.parseInt(progArgs[0]);
        } catch (UnknownHostException e) {
            System.err.println("Error: unable to determine machine hostname. " + e.getMessage());
            return;
        }

        System.out.println("Registry starting at " + myIP + ":" + myPort);

        setState(RegistryState.REGISTRATION);

        try {
            tcpServer = new TCPServerThread(Integer.parseInt(progArgs[0]), connectionManager, this);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error starting TCP listener on port " + progArgs[0]);
            return;
        }

        serverThread = new Thread(tcpServer);
        serverThread.start();

        helper = new RegistryHelperThread();
        helperThread = new Thread(helper);
        helperThread.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (getState() != RegistryState.EXITING) {
            try {

                String line = input.readLine();
                String[] command = line.split(" ");

                if (command[0].equals("exit")) {
                    if (getState() != RegistryState.REGISTRATION) {
                        System.out.println("It's too late for that now");
                        continue;
                    }
                    // probably ought to tell the messiging nodes we're going down
                    setState(RegistryState.EXITING);
                    helperThread.interrupt();
                    break;

                } else if (command[0].equals("list-messaging-nodes")) {
                    System.out.println(overlay.getCount() + " currently registered nodes:\n\t(ip)\t\t\t(port)");
                    for (String[] node : overlay.getNodes()) {
                        System.out.println("\t" + node[0] + "\t" + node[1]);
                    }

                } else if (command[0].equals("list-weights")) {
                    if (! (getState() == RegistryState.SEND_WEIGHTS || getState() == RegistryState.TASK_STARTED
                            || getState() == RegistryState.PRINT_STATS || getState() == RegistryState.PULL_TRAFFIC)) {

                        System.out.println("Weights have not yet been generated. "
                                + "You need to call send-overlay-link-weights first");
                        continue;
                    }

                    ArrayList<String[]> cons = overlay.getConnectionWeights();

                    System.out.println("connection weights");
                    for (String[] con : cons) {
                        System.out.println("\t" + con[0] + "\t" + con[1] + "\t" + con[2]);
                    }

                } else if (command[0].equals("setup-overlay")) {
                    if (getState() != RegistryState.REGISTRATION) {
                        System.out.println("The overlay has already been created.");
                        continue;
                    }

                    int numConnections = 0;

                    boolean badArg = false;
                    if (command.length == 1) {
                        numConnections = 4;
                    } else if (command.length != 2) {
                        badArg = true;
                    }
                    else {
                        try {
                            numConnections = Integer.parseInt(command[1]);
                        } catch (NumberFormatException e) {
                            badArg = true;
                        }
                    }

                    if (numConnections < 1) {
                        badArg = true;
                    }

                    if (badArg) {
                        System.out.println("usage: setup-overlay number-of-connections");
                        continue;
                    }

                    setupOverlay(numConnections);



                } else if (command[0].equals("send-overlay-link-weights")) {
                    if (getState() != RegistryState.CREATE_OVERLAY) {
                        System.out.println("Overlay hasn't been created yet or weights have already been sent.");
                        continue;
                    }
                    sendWeights();

                    // todo: do more stuff

                } else if (command[0].equals("start")) {
                    if (getState() != RegistryState.SEND_WEIGHTS) {
                        System.out.println("We are not ready to start, or have already started.");
                        continue;
                    }
                    boolean badArg = false;
                    if (command.length != 2) {
                        badArg = true;
                    }
                    int numRounds = 0;
                    try {
                        numRounds = Integer.parseInt(command[1]);
                    } catch (NumberFormatException e) {
                        badArg = true;
                    }

                    if (badArg) {
                        System.out.println("usage: setup-overlay number-of-connections (default 4)");
                        continue;
                    }

                    taskStart(numRounds);

                } else {
                    System.out.println("Invalid command. valid commands are: \n"
                            + "\tlist-messaging-nodes\n"
                            + "\tlist-weights\n"
                            + "\tsetup-overlay number-of-connections (default 4)\n"
                            + "\tsend-overlay-link-weights\n"
                            + "\texit");
                    continue;
                }
            } catch (Exception e) {
                System.err.println("Error: Exception handling user input");
                System.err.println(e.getMessage());
                continue;
            }
        }


        try {
            tcpServer.stop();
            helperThread.interrupt();

            serverThread.join();
            helperThread.join();

            connectionManager.closeAllConnections();

        } catch (InterruptedException e) {
            System.err.println("Error: interrupted while joining helper threads");
            return;
        } catch (IOException e) {
            System.err.println("Error: IOException while stopping server thread.");
            System.err.println(e.getMessage());
            return;
        }
    }

    public static void main(String[] args) {
        Registry registry = new Registry(args);
        registry.startRegistry();
    }
}
