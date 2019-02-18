package cs455.overlay.node;

import cs455.overlay.events.*;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MessagingNode implements Node {

    private enum NodeState {
        REGISTERING, REGISTERED, DEREGISTERING, CONNECTING, ROUTING, WORKING, TASK_COMPLETE, SENDING_SUMMARY, EXITING;
    }

    private String[] progArgs;

    private String myIP;
    private int myPort;

    private String registryAddress;
    private int registryPort;
    private int registryID;

    private MessagingNodeHelper helper;
    private Thread helperThread;

    private TCPServerThread tcpServer;
    private Thread serverThread;

    private NodeState nodeState;

    private ConnectionManager connectionManager;

    private LinkedBlockingQueue<Event> eventQueue;

    private HashMap<String, Integer> connectionLookup;


    public MessagingNode(String[] args) {
        progArgs = args;
        nodeState = NodeState.REGISTERING;
        connectionManager = new ConnectionManager(this);
        eventQueue = new LinkedBlockingQueue<>();
    }

    private synchronized NodeState getState() {
        return nodeState;
    }

    private synchronized void setState(NodeState state) {
        nodeState = state;
    }

    private void printHelp() {
        System.out.println("Usage: messageNode <registry address> <registry port>");
    }

    private class MessagingNodeHelper implements Runnable {

        @Override
        public void run() {

            System.out.println("Message node helper thread starting");

            while (getState() != NodeState.EXITING) {
                try {

                    Event e = eventQueue.take();

                    switch (e.getType()) {
                        case REGISTER_RESPONSE:
                            handleRegisterResponse((RegisterResponse) e);
                            break;
                        case DEREGISTER_RESPONSE:
                            handleDeregisterResponse((DeregisterResponse) e);
                            break;
                        case MESSAGING_NODES_LIST:
                            handleMessagingNodesList((MessagingNodesList) e);

                    }
                } catch (InterruptedException e) {
                    System.err.println("Helper thread interrupted");
                    break;
                } catch (IOException e) {
                    System.err.println("IOError in helper thread: " + e.getMessage());
                }
            }

            System.out.println("Message node helper thread exiting");
        }
    }

    @Override
    public void onEvent(Event event) {
        eventQueue.offer(event);
    }

    private void handleMessagingNodesList(MessagingNodesList nodesList) throws IOException{
        connectionLookup = new HashMap<>();

        int numNodes = nodesList.getNumNodes();
        String nodeList = nodesList.getNodeList();
        String []connections = nodeList.split(",");

        if (connections.length != numNodes) {
            System.err.println("Received bad message nodes list from registry.");
            return;
        }

        for (String con : connections) {
            String []conInfo = con.split(":");
            if (conInfo.length != 2) {
                System.err.println("Received bad message nodes list from registry.");
                return;
            }

            // todo : figure out how to store connections in messaging node
            int conID = connectionManager.newConnection(conInfo[0], Integer.parseInt(conInfo[1]));
            connectionLookup.put(conInfo[0] + ":" + conInfo[1], conID);
        }

        System.out.println("Connected to " + connectionLookup.size() + " peers.");

    }

    private void handleDeregisterResponse(DeregisterResponse response) {
        System.out.println("Deregistration from registry " + (response.getSuccess() ? "successful" : "failed") + ". ("
                + response.getInfo() + "). " + response.getNumberRegistered() + " nodes currently registered on registry.");
        setState(NodeState.EXITING);
    }

    private void handleRegisterResponse(RegisterResponse response) {
        if (response.getSuccess()) {
            System.out.println("Successfully registered to registry at " + response.getOrigin() + ". "
                    + response.getRegisterCount() + " nodes registered at this time.");

            setState(NodeState.REGISTERED);

        } else {
            System.out.println("Registration was not successful with registry at " + response.getOrigin()
                    + ". Reason: " + response.getInfo());

            setState(NodeState.EXITING);
        }

    }

    public void startMessageNode() {

        if (progArgs.length != 2) {
            printHelp();
            System.exit(1);
        }

        setState(NodeState.REGISTERING);

        try {
            registryAddress = progArgs[0];
            registryPort = Integer.parseInt(progArgs[1]);

            registryID = connectionManager.newConnection(registryAddress, registryPort);
        } catch (IOException e) {
            System.err.println("Error: could not newConnection to registry on " + registryAddress + ":" + registryPort);
            System.err.println(e.getMessage());
            return;
        }


        try {
            tcpServer = new TCPServerThread(connectionManager, this);
            myPort = tcpServer.getPort();
            myIP = InetAddress.getLocalHost().getHostAddress();


            System.out.println("Messaging node running on " + myIP + ":" + myPort);

            System.out.println("Attempting to register with registry at " + registryAddress + ":" + registryPort);

            connectionManager.sendMessage(registryID, new RegisterRequest(myIP, myPort, "localhost", -1));

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        serverThread = new Thread(tcpServer);
        serverThread.start();

        helper = new MessagingNodeHelper();
        helperThread = new Thread(helper);
        helperThread.start();

        /* Command loop */

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (getState() != NodeState.EXITING) {
            try {

                String command = input.readLine();

                if (command.equals("exit-overlay")) {
                    setState(NodeState.DEREGISTERING);
                    DeregisterRequest request = new DeregisterRequest(myIP, myPort, "localhost", -1);
                    connectionManager.sendMessage(registryID, request);
                    System.out.println("Send deregistration request, waiting 3 seconds for response");
                    Thread.sleep(3000);
                    break;
                } else if (command.equals("print-shortest-path")) {
                    // print dat path
                } else {
                    System.out.println("Invalid command. Valid commands are:\n"
                            + "\tprint-shortest-path\n"
                            + "\texit-overlay");
                }
            } catch (IOException e) {
                System.err.println("Error: IOException while reading user input " + e.getMessage());
                break;
            } catch (InterruptedException e) {
                System.err.println("Error: interrupted while waiting for deregister response " + e.getMessage());
                break;
            }
        }

        System.out.println("Exited command loop");

        try {
            tcpServer.stop();

            serverThread.join();

            helperThread.interrupt();

            helperThread.join();
        } catch (InterruptedException e) {
            System.err.println("Error: interrupted while stopping helper thread");
            System.err.println(e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Error: IOException while stopping helper threads (server thread)");
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("Helper threads joined, bye!");


    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args);
        node.startMessageNode();
    }
}
