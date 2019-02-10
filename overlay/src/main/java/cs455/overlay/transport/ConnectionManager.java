package cs455.overlay.transport;

import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConnectionManager {
    private class Connection {
        protected String identifier;
        protected InetAddress address;
        protected int port;
        protected Socket socket;
        protected DataInputStream in;
        protected DataOutputStream out;
        protected TCPReceiverThread receiver;
        protected Thread receiverThread;
    }


    private ArrayList<Connection> connections;


    public ConnectionManager() {
        connections = new ArrayList<>();
    }

    public String newConnection(Socket socket, TCPReceiverThread receiver, Thread thread)
            throws IOException {
        Connection connection = new Connection();
        connection.identifier = socket.getInetAddress().toString() + ':' + socket.getLocalPort();
        connection.address = socket.getInetAddress();
        connection.port = socket.getPort();
        connection.socket = socket;
        connection.in = new DataInputStream(socket.getInputStream());
        connection.out = new DataOutputStream(socket.getOutputStream());
        connection.receiver = receiver;
        connection.receiverThread = thread;

        synchronized (this) {
            connections.add(connection);
        }

        return connection.identifier;
    }

    public void sendMessage(String connectionID, Event message) throws IOException {
        for (Connection c : connections) {
            if (c.identifier.equals(connectionID)) {
                byte []bytes = message.getBytes();
                c.out.write(bytes.length);
                c.out.write(message.getBytes());
                c.out.flush();
                return;
            }
        }
        throw new NoSuchElementException("Error: no connection " + connectionID);
    }

    public ArrayList<String> getConnectionList() {
        ArrayList<String> clist = new ArrayList<>();
        for (Connection c : connections) {
            clist.add(c.identifier);
        }

        return clist;
    }

    // TODO: KeyException is inappropriate
    public void closeConnection(String id) throws IOException, InterruptedException {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext(); ) {
                Connection c = i.next();
                if (c.identifier.equals(id)) {
                    c.receiver.stop();
                    c.receiverThread.join();
                    c.out.close();
                    c.in.close();
                    c.socket.close();

                    i.remove();
                    return;
                }
            }
            throw new NoSuchElementException("Error: no connection " + id);
        }
    }
}
