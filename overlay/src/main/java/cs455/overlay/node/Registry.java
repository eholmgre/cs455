package cs455.overlay.node;

import cs455.overlay.events.*;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Registry implements Node {

    protected class MessagingNodeHandle {
        String id;
        String ip;
        int port;
        int connectionId;
        ArrayList<String> connections;

        public MessagingNodeHandle(String id, String ip, int port, int connectionId) {
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.connectionId = connectionId;
            connections = new ArrayList<>();
        }
    }

    protected class RegistryHelperThread implements Runnable {

        private volatile boolean stopped;

        private synchronized boolean beenStopped() {
            return stopped;
        }

        public synchronized void stop() {
            stopped = true;
        }

        private synchronized Registry.RegistryState getState() {
            return registryState;
        }

        public RegistryHelperThread() {
            stopped = false;
        }

        @Override
        public void run() {
            System.out.println("Registry helper thread starting.");
            while (!beenStopped()) {
                try {
                        Event e = eventQueue.take();

                        switch (e.getType()) {
                            case REGISTER_REQUEST:
                                handleRegisterRequest((RegisterRequest) e);
                                break;
                            case DEREGISTER_REQUEST:
                                handleDeregisterRequest((DeregisterRequest) e);
                                break;
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
        REGISTRATION, CREATE_OVERLAY, TASK_STARTED, PULL_TRAFFIC, PRINT_STATS, EXITING
    }

    private String[] progArgs;

    private RegistryState registryState;

    private String myIP;
    private int myPort;

    private ArrayList<MessagingNodeHandle> messagingNodes;

    private TCPServerThread tcpServer;
    private Thread serverThread;

    private RegistryHelperThread helper;
    private Thread helperThread;

    private ConnectionManager connectionManager;

    private LinkedBlockingQueue<Event> eventQueue;

    public Registry(String[] args) {
        progArgs = args;
        registryState = RegistryState.REGISTRATION;
        messagingNodes = new ArrayList<>();
        connectionManager = new ConnectionManager(this);
        eventQueue = new LinkedBlockingQueue<>();
    }

    private synchronized RegistryState getState() {
        return registryState;
    }

    private synchronized void setState(RegistryState state) {
        registryState = state;
    }

    private void handleRegisterRequest(RegisterRequest e) throws IOException{
        boolean success = true;
        String info = "";

        String requestId = e.getIp() + ':' + e.getPort();

        int connectioID = e.getConnectionId();

        if (! e.getIp().equals(e.getOrigin())) {
            // Error: request origin and ip do not match
            success = false;
            info = "Request ip field does not match request origin";
        }

        for (MessagingNodeHandle node : messagingNodes) {
            if (node.id.equals(requestId)) {
                success = false;
                info = "A node is currently registered on this ip and port";
            }

        }

        System.out.println("Received registration request from " + requestId
                + ". Registration " + (success ? "successful." : "failed (" + info + ")."));

        int registerCount = messagingNodes.size();

        if (success) {
            info = "Welcome aboard";
            messagingNodes.add(new MessagingNodeHandle(requestId, e.getIp(), e.getPort(), connectioID));
        }

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

        boolean found = false;
        for (MessagingNodeHandle node : messagingNodes) {
            if (node.id.equals(requestId)) {
                found = true;
            }
        }

        if (! found) {
            success = false;
            info = "No node found with id " + requestId;
        }

        if (! e.getIp().equals(e.getOrigin())) {
            // Error: request origin and ip do not match
            success = false;
            info = "Request ip field does not match request origin";
        }

        System.out.println("Received deregistration request from " + requestId
                + ". Deregistration " + (success ? "successful." : "failed (" + info + ")."));

        if (success) {
            info = "well, bye";

            messagingNodes.removeIf(h -> h.id.equals(requestId));
        }

        int registerCount = messagingNodes.size();

        DeregisterResponse response = new DeregisterResponse(success, registerCount, info, "localhost", connectionId);

        connectionManager.sendMessage(connectionId, response);

        connectionManager.closeConnection(connectionId);

        System.out.println("Deregistration " + (success ? "successful" : "failed") + " for node " + requestId
                + " (" + info + "). " + registerCount + " nodes registered.");
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

                String command = input.readLine();

                if (command.toLowerCase().equals("exit")) {
                    setState(RegistryState.EXITING);
                    helperThread.interrupt();
                    break;
                }
            } catch (IOException e) {
                System.err.println("Error: IOException while reading user input");
                System.err.println(e.getMessage());
                break;
            }
        }


        try {
            tcpServer.stop();
            helper.stop();

            serverThread.join();
            helperThread.join();
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
