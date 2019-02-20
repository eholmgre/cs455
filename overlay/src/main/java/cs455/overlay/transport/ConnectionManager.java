package cs455.overlay.transport;

import cs455.overlay.events.Event;
import cs455.overlay.node.Node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConnectionManager {
    private class Connection {
        protected int identifier;
        //protected String address;
        //protected int port;
        protected Socket socket;
        protected TCPSender sender;
        protected TCPReceiverThread receiver;
        protected Thread receiverThread;
        protected String nodeId;
    }


    private ArrayList<Connection> connections;

    private Node parent;

    private int idCounter;


    public ConnectionManager(Node parent) {
        this.parent = parent;
        connections = new ArrayList<>();
        idCounter = 0;
    }

    public int getConnectionId(String nodeId) throws NoSuchElementException {
        for (Connection c : connections) {
            if (nodeId.equals(c.nodeId)) {
                return c.identifier;
            }
        }

        throw new NoSuchElementException();
    }

    public int newConnection(String address, int port) throws IOException {
        Connection connection = new Connection();
        connection.identifier = idCounter++;
        connection.nodeId = address + ":" + port;
        connection.socket = new Socket(address, port);
        //connection.address = connection.socket.getInetAddress().getHostAddress();
        //connection.port = connection.socket.getPort();
        connection.sender = new TCPSender(connection.socket);
        connection.receiver = new TCPReceiverThread(connection.socket, this, parent);
        connection.receiverThread = new Thread(connection.receiver);

        connection.receiverThread.start();

        connections.add(connection);

        return connection.identifier;

    }


    public int addConnection(Socket socket, TCPReceiverThread receiver, Thread thread)
            throws IOException {
        Connection connection = new Connection();
        connection.identifier = idCounter++;
        connection.nodeId = null;
        //connection.address = socket.getInetAddress().getHostAddress();
        //connection.port = connection.socket.getPort();
        connection.socket = socket;
        connection.sender = new TCPSender(socket);
        connection.receiver = receiver;
        connection.receiverThread = thread;

        connections.add(connection);

        return connection.identifier;
    }

    public void setNodeId(int connectionId, String nodeId) throws NoSuchElementException {
        for (Connection c : connections) {
            if (c.identifier == connectionId) {
                if (c.nodeId == null) {
                    c.nodeId = nodeId;
                } else {
                    System.err.println("NodeID has already been set for connection " + c.identifier + " (" + c.nodeId + ").");
                }
                return;
            }
        }

        throw new NoSuchElementException("setNodeID didnt find connection");
    }


    public void sendMessage(int connectionID, Event message) throws IOException {
        for (Connection c : connections) {
            if (c.identifier == connectionID) {
                byte[] bytes = message.getBytes();
                synchronized (c.sender) { //todo: figure out if this is a big deal
                    c.sender.sendData(bytes);
                }
                return;
            }
        }
        throw new NoSuchElementException("Error: no connection " + connectionID);
    }

    public void broadcast(Event message) throws IOException {
        for (Connection c : connections) {
            sendMessage(c.identifier, message);
        }
    }

    public void connectionClosing(int id) throws IOException {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext(); ) {
                Connection c = i.next();
                if (c.identifier == id) {
                    if (c.identifier == 0) {
                        System.err.println("Disconnected from registry");
                    }
                    c.socket.close();
                    i.remove();
                    System.out.println("removed closed connection");
                    return;
                }
            }
            //throw new NoSuchElementException("Error: no connection " + id);
        }
    }

    public void closeConnection(int id) throws IOException, InterruptedException {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext(); ) {
                Connection c = i.next();
                if (c.identifier == id) {
                    c.receiver.close();
                    c.socket.close();
                    c.receiverThread.join();
                    i.remove();
                    return;
                }
            }
            throw new NoSuchElementException("Error: no connection " + id);
        }
    }

    public void closeAllConnections() throws IOException, InterruptedException {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext(); ) {
                Connection c = i.next();
                c.receiver.close();
                c.socket.close();
                System.out.println("Attempting to stop received thread for " + c.identifier);
                c.receiverThread.join();
                System.out.println("Joined received thread for " + c.identifier);
                i.remove();
            }
        }

    }
}
