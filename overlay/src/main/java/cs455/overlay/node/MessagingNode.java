package cs455.overlay.node;

import cs455.overlay.events.Event;
import cs455.overlay.events.RegisterRequest;
import cs455.overlay.events.RegisterResponse;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MessagingNode implements Node {

    private enum NodeState {
        REGISTERING, REGISTERED, DEREGISTERING, CONNECTING, ROUTING, WORKING, TASK_COMPLETE, SENDING_SUMMARY, EXITING;
    }

    private String[] progArgs;

    private String myIP;
    private int myPort;

    private String registryAddress;
    private int registryPort;
    private String registryID;

    private MessagingNodeHelper helper;
    private Thread helperThread;

    private TCPServerThread tcpServer;
    private Thread serverThread;

    private NodeState nodeState;

    private ConnectionManager connections;

    private ConcurrentLinkedQueue<Event> eventQueue;


    //public MessagingNode(String address, int port) {
    public MessagingNode(String[] args) {
        progArgs = args;
        nodeState = NodeState.REGISTERING;
        connections = new ConnectionManager(this);
        eventQueue = new ConcurrentLinkedQueue<>();
    }

    private void printHelp() {
        System.out.println("Usage: messageNode <registry address> <registry port>");
    }

    private class MessagingNodeHelper implements Runnable {

        private volatile boolean stopped;

        /*
        Might need this if we need to synchronize access to stop flag.

        private synchronized boolean beenStopped() {
            return stopped;
        }
        */

        public synchronized void stop() {
            stopped = true;
        }

        @Override
        public void run() {

            while (!stopped) {
                //try {
                if (!eventQueue.isEmpty()) {
                    Event e = eventQueue.poll();

                    switch (e.getType()) {
                        case REGISTER_RESPONSE:
                            handleRegisterResponse((RegisterResponse) e);
                            break;
                    }
                }
                //} catch (Exception e) {
                //    System.out.println("Error in helper thread: " + e.getMessage());
                //}

                if (nodeState == NodeState.EXITING) {
                    return;
                }
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        eventQueue.add(event);

    }

    private void handleRegisterResponse(RegisterResponse response) {
        if (response.getSuccess()) {
            System.out.println("Successfully registered to registry at " + response.getOrigin() + ". "
                    + response.getRegisterCount() + " other nodes registered at this time.");

            //TODO: maybe synchronize?
            this.nodeState = NodeState.REGISTERED;

        } else {
            System.out.println("Registration was not successful with registry at " + response.getOrigin()
                    + ". Reason: " + response.getInfo());

            this.nodeState = NodeState.EXITING;
        }

    }

    public void startMessageNode() {

        if (progArgs.length != 2) {
            printHelp();
            System.exit(1);
        }


        nodeState = NodeState.REGISTERING;


        try {
            registryAddress = progArgs[0];
            registryPort = Integer.parseInt(progArgs[1]);

            registryID = connections.newConnection(registryAddress, registryPort);
        } catch (IOException e) {
            System.out.println("Error: could not newConnection to registry on " + registryAddress + ":" + registryPort);
            System.out.println(e.getMessage());
            return;
        }


        try {
            tcpServer = new TCPServerThread(connections, this);
            myPort = tcpServer.getPort();
            myIP = InetAddress.getLocalHost().getHostAddress();


            System.out.println("Messaging node running on " + myIP + ":" + myPort);

            System.out.println("Attempting to register with registry at " + registryAddress + ":" + registryPort);

            connections.sendMessage(registryID, new RegisterRequest(registryAddress, registryPort, "localhost"));

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        serverThread = new Thread(tcpServer);
        serverThread.start();

        helper = new MessagingNodeHelper();
        helperThread = new Thread(helper);
        helperThread.start();

        /* Command loop */

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (nodeState != NodeState.EXITING) {
            try {

                String command = input.readLine();

                if (command.toLowerCase().equals("exit")) {
                    nodeState = NodeState.EXITING;
                    break;
                }
            } catch (IOException e) {
                System.out.println("Error: IOException while reading user input");
                System.out.println(e.getMessage());
                break;
            }
        }


        tcpServer.stop();
        helper.stop();


        try {
            serverThread.join();
            helperThread.join();
        } catch (InterruptedException e) {
            System.out.println("Error: interrupted while stopping helper thread");
            System.out.println(e.getMessage());
            return;
        }


    }

    public static void main(String[] args) {
        MessagingNode node = new MessagingNode(args);
        node.startMessageNode();
    }
}
