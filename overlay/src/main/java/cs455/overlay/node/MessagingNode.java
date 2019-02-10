package cs455.overlay.node;

import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;
import cs455.overlay.events.RegisterRequest;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.util.ServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// TODO: should this be a singleton?
public class MessagingNode implements Node{

    private enum NodeState {
        REGISTERING, REGISTERED, DEREGISTERING, CONNECTING, ROUTING, WORKING, TASK_COMPLETE, SENDING_SUMMARY;
    }

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


    public MessagingNode(String address, int port) {
        this.registryAddress = address;
        this.registryPort = port;
        state = NodeState.REGISTERING;
        connections = new ConnectionManager();
        messageFactory = EventFactory.getInstance();
    }

    private static void printHelp() {
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

    private void stopHelper() {
        helper.stop();
    }

    private String createConnection(String address, int port) throws IOException {
        Socket soc = new Socket(address, port);
        TCPReceiverThread receiver = new TCPReceiverThread(soc);
        Thread receiverThread = new Thread(receiver);

        return connections.newConnection(soc, receiver, receiverThread);
    }

    // wow this is a pointless method
    private void sendMessage(String connectionID, Event message) throws IOException{
        connections.sendMessage(connectionID, message);
    }

    @Override
    public void onEvent(Event event) {

    }

    public static void main(String[] args) {
        if (args.length != 2) {
            MessagingNode.printHelp();
            System.exit(1);
        }


        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));

        try {
            node.registryID = node.createConnection(node.registryAddress, node.registryPort);
        } catch (IOException e) {
            System.out.println("Error: could not connect to registry on " + node.registryAddress + ":" + node.registryPort);
            System.out.println(e.getMessage());
        }


        try {
            node.serverSocket = node.getServerSocket.makeServerSocket();
            node.myPort = node.getServerSocket.getPort();
            node.myIP = node.serverSocket.getInetAddress().toString(); // aww hell what does this even do?


            node.connections.sendMessage(node.registryID, new RegisterRequest(node.myIP, node.registryPort));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        node.startHelper();




        node.stopHelper();


    }
}
