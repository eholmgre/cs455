package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionManager {
    private class Connection {
        public String identifier;
        public InetAddress address;
        public int port;
        public Socket socket;
        public DataInputStream in;
        public DataOutputStream out;
        public TCPReceiverThread receiver;
        public Thread receiverThread;
    }

    private ArrayList<Connection> connections;


    public ConnectionManager() {
        connections = new ArrayList<>();
    }

    public void newConnection(Socket socket, TCPReceiverThread receiver, Thread thread)
    throws Exception{
        synchronized (this) {
            Connection connection = new Connection();
            connection.identifier = socket.getInetAddress().toString() + ':' + socket.getLocalPort();
            connection.address = socket.getInetAddress();
            connection.port = socket.getPort();
            connection.socket = socket;
            connection.in = new DataInputStream(socket.getInputStream());
            connection.out = new DataOutputStream(socket.getOutputStream());
            connection.receiver = receiver;
            connection.receiverThread = thread;

            connections.add(connection);
        }
    }

    public void closeConnection(String id) throws KeyException, Exception {
        synchronized (this) {
            for (Iterator<Connection> i = connections.iterator(); i.hasNext();) {
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
            throw new KeyException("Error: no connection " + id);
        }
    }
}
