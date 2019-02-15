package cs455.overlay.node;

import cs455.overlay.events.RegisterRequest;
import cs455.overlay.events.RegisterResponse;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.events.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Registry implements Node {

    protected class MessagingNodeHandle {
        String id;
        String ip;
        int port;
        ArrayList<String> connections;

        public MessagingNodeHandle(String id, String ip, int port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
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
            while (!beenStopped()) {
                try {
                    if (! eventQueue.isEmpty()) {
                        Event e = eventQueue.poll();

                        switch (e.getType()) {
                            case REGISTER_REQUEST:
                                handleRegisterRequest((RegisterRequest) e);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error in registry helper thread: " + e.getMessage());
                    return;
                }

            }

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

    private ConcurrentLinkedQueue<Event> eventQueue;

    public Registry(String[] args) {
        progArgs = args;
        registryState = RegistryState.REGISTRATION;
        messagingNodes = new ArrayList<>();
        connectionManager = new ConnectionManager();
        eventQueue = new ConcurrentLinkedQueue<>();


    }

    private void handleRegisterRequest(RegisterRequest e) throws IOException{
        boolean success = true;
        String info = "";

        String requestId = e.getIp() + ':' + e.getPort();

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
            messagingNodes.add(new MessagingNodeHandle(requestId, e.getIp(), e.getPort()));
        }

        RegisterResponse response = new RegisterResponse(success, info, registerCount, "localhost");

        connectionManager.sendMessage(requestId, response);

        System.out.println("Sent registration response to " + requestId + ".");
    }


    @Override
    public void onEvent(Event event) {
        eventQueue.add(event);

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
            System.out.println("Error: unable to determine machine hostname. " + e.getMessage());
            return;
        }

        System.out.println("Registry starting at " + myIP + ":" + myPort);

        registryState = RegistryState.REGISTRATION;

        try {
            tcpServer = new TCPServerThread(Integer.parseInt(progArgs[0]), connectionManager);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error starting TCP listener on port " + progArgs[0]);
            return;
        }

        serverThread = new Thread(tcpServer);
        serverThread.start();

        RegistryHelperThread registryHelper = new RegistryHelperThread();
        Thread helperThread = new Thread(registryHelper);
        helperThread.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        while (registryState != RegistryState.EXITING) {
            try {

                String command = input.readLine();

                if (command.toLowerCase().equals("exit")) {
                    registryState = RegistryState.EXITING;
                    break;
                }
            } catch (IOException e) {
                System.out.println("Error: IOException while reading user input");
                System.out.println(e.getMessage());
                break;
            }
        }

        tcpServer.stop();
        registryHelper.stop();

        try {
            serverThread.join();
            helperThread.join();
        } catch (InterruptedException e) {
            System.out.println("Error: interrupted while joining helper threads");
            return;
        }


    }

    public static void main(String[] args) {
        Registry registry = new Registry(args);
        registry.startRegistry();
    }
}
