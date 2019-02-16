package cs455.overlay.node;

import cs455.overlay.events.*;
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
    private int registryID;

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

            System.out.println("Message node helper thread starting");

            while (!stopped) {
                //try {
                if (!eventQueue.isEmpty()) {
                    Event e = eventQueue.poll();

                    switch (e.getType()) {
                        case REGISTER_RESPONSE:
                            handleRegisterResponse((RegisterResponse) e);
                            break;
                        case DEREGISTER_RESPONSE:
                            handleDeregisterResponse((DeregisterResponse) e);
                            break;

                    }
                }
                //} catch (Exception e) {
                //    System.out.println("Error in helper thread: " + e.getMessage());
                //}

                if (nodeState == NodeState.EXITING) {
                    break;
                }
            }

            System.out.println("Message node helper thread exiting");
        }
    }

    @Override
    public void onEvent(Event event) {
        eventQueue.add(event);
    }

    private void handleDeregisterResponse(DeregisterResponse response) {
        System.out.println("Deregistration from registry " + (response.getSuccess() ? "successful" : "failed") + ". ("
        + response.getInfo() + "). " + response.getNumberRegistered() + " nodes currently registered on registry.");
        nodeState = NodeState.EXITING;
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
            System.err.println("Error: could not newConnection to registry on " + registryAddress + ":" + registryPort);
            System.err.println(e.getMessage());
            return;
        }


        try {
            tcpServer = new TCPServerThread(connections, this);
            myPort = tcpServer.getPort();
            myIP = InetAddress.getLocalHost().getHostAddress();


            System.out.println("Messaging node running on " + myIP + ":" + myPort);

            System.out.println("Attempting to register with registry at " + registryAddress + ":" + registryPort);

            connections.sendMessage(registryID, new RegisterRequest(myIP, myPort, "localhost", -1));

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
        while (nodeState != NodeState.EXITING) {
            try {

                String command = input.readLine();

                if (command.toLowerCase().equals("exit") || command.toLowerCase().equals("deregister")) {
                    nodeState = NodeState.DEREGISTERING;
                    DeregisterRequest request = new DeregisterRequest(myIP, myPort, "localhost", -1);
                    connections.sendMessage(registryID, request);
                    System.out.println("Send deregistration request, waiting 3 seconds for response");
                    Thread.sleep(3000);
                    break;
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
            helper.stop();

            serverThread.join();
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
