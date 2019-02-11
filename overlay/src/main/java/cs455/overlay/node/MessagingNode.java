package cs455.overlay.node;

import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;
import cs455.overlay.events.RegisterRequest;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.util.ServerSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class MessagingNode implements Node{

    private enum NodeState {
        REGISTERING, REGISTERED, DEREGISTERING, CONNECTING, ROUTING, WORKING, TASK_COMPLETE, SENDING_SUMMARY;
    }

    private String[] progArgs;

    private String registryAddress;
    private int registryPort;
    private String registryID;
    private MessagingNodeHelper helper;
    private Thread helperThread;

    private String myIP;
    private int myPort;

    private ServerSocket serverSocket;

    private NodeState state;

    private ConnectionManager connections;

    private ServerSocketFactory getServerSocket;

    private EventFactory messageFactory;


    //public MessagingNode(String address, int port) {
    public MessagingNode(String []args) {
        progArgs = args;
        state = NodeState.REGISTERING;
        connections = new ConnectionManager();
        messageFactory = EventFactory.getInstance();
        getServerSocket = new ServerSocketFactory();
    }

    private void printHelp() {
        System.out.println("Usage: messageNode <registry address> <registry port>");
    }

    private class MessagingNodeHelper implements Runnable {

        private volatile boolean stopped;

        private synchronized boolean beenStopped() {
            return stopped;
        }

        public synchronized void stop() {
            stopped = true;
        }

        @Override
        public void run() {

            while (! beenStopped()) {

            }

        }
    }

    private void startHelper() {
        helper = new MessagingNodeHelper();
        helperThread = new Thread(helper);
        helperThread.start();
    }

    private void stopHelper() throws InterruptedException{
        helper.stop();
        helperThread.join();
    }

    private String createConnection(String address, int port) throws IOException {
        Socket soc = new Socket(address, port);
        TCPReceiverThread receiver = new TCPReceiverThread(soc);
        Thread receiverThread = new Thread(receiver);

        return connections.newConnection(soc, receiver, receiverThread);
    }

    @Override
    public void onEvent(Event event) {

    }

    public void startMessageNode() {

        if (progArgs.length != 2) {
            printHelp();
            System.exit(1);
        }


        try {
            registryAddress = progArgs[0];
            registryPort = Integer.parseInt(progArgs[1]);

            registryID = createConnection(registryAddress, registryPort);
        } catch (IOException e) {
            System.out.println("Error: could not connect to registry on " + registryAddress + ":" + registryPort);
            System.out.println(e.getMessage());
            return;
        }


        try {
            serverSocket = getServerSocket.makeServerSocket();
            myPort = getServerSocket.getPort();
            myIP = InetAddress.getLocalHost().getHostAddress();

            System.out.println("Messaging node running on " + myIP + ":" + myPort);

            connections.sendMessage(registryID, new RegisterRequest(myIP, registryPort));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        startHelper();




        try {
            stopHelper();
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
