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
        protected String address;
        protected int port;
        protected Socket socket;
        protected TCPSender sender;
        protected TCPReceiverThread receiver;
        protected Thread receiverThread;
    }


    private ArrayList<Connection> connections;

    private Node parent;

    private int idCounter;


    public ConnectionManager(Node parent) {
        this.parent = parent;
        connections = new ArrayList<>();
        idCounter = 0;
    }

    public int newConnection(String address, int port) throws IOException {
        Connection connection = new Connection();
        connection.identifier = idCounter++;
        connection.socket = new Socket(address, port);
        connection.address = connection.socket.getInetAddress().getHostAddress();
        connection.port = connection.socket.getPort();
        connection.sender = new TCPSender(connection.socket);
        connection.receiver = new TCPReceiverThread(connection.socket, parent);
        connection.receiverThread = new Thread(connection.receiver);

        connection.receiverThread.start();

        connections.add(connection);

        return connection.identifier;

    }


    public int addConnection(Socket socket, TCPReceiverThread receiver, Thread thread)
            throws IOException {
        Connection connection = new Connection();
        connection.identifier = idCounter++;
        connection.address = socket.getInetAddress().getHostAddress();
        connection.port = socket.getPort();
        connection.socket = socket;
        connection.sender = new TCPSender(socket);
        connection.receiver = receiver;
        connection.receiverThread = thread;

        connections.add(connection);

        return connection.identifier;
    }


    public void sendMessage(int connectionID, Event message) throws IOException {
        for (Connection c : connections) {
            if (c.identifier == connectionID) {
                byte []bytes = message.getBytes();
                c.sender.sendData(bytes);
                return;
            }
        }
        throw new NoSuchElementException("Error: no connection " + connectionID);
    }

    public void closeConnection(int id) throws IOException, InterruptedException {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext(); ) {
                Connection c = i.next();
                if (c.identifier == id) {
                    c.socket.close();
                    System.out.println("Attempting to stop received thread for " + c.identifier);
                    c.receiverThread.join();
                    System.out.println("Joined received thread for " + c.identifier);
                    i.remove();
                    return;
                }
            }
            throw new NoSuchElementException("Error: no connection " + id);
        }
    }
}
