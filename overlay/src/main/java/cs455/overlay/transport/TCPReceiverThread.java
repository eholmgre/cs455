package cs455.overlay.transport;

import cs455.overlay.events.EventFactory;
import cs455.overlay.node.Node;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

// will running event processing operations in this thread cause lost performance/ packets ?
public class TCPReceiverThread implements Runnable{

    private Socket socket;
    private DataInputStream din;
    private EventFactory messageFactory;

    private String origin;

    private Node parent;

    private ConnectionManager connectionManager;

    private int connectionId;

    private volatile boolean closing;

    private synchronized boolean beenStopped() {
        return this.closing;
    }

    public synchronized void close() {
        this.closing = true;
    }

    public TCPReceiverThread(Socket socket,ConnectionManager cm, Node parent) throws IOException {
        this.socket = socket;
        this.parent = parent;
        din = new DataInputStream(socket.getInputStream());
        closing = false;
        messageFactory = EventFactory.getInstance();
        origin = socket.getInetAddress().getHostAddress();
        connectionManager = cm;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void run() {

        int dataLength;
        while (socket != null && ! socket.isClosed()) { // todo: probably can simplify
            try {
                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);

                parent.onEvent(messageFactory.createEvent(data, origin, connectionId));

            } catch (IOException e) { // includes SocketException
                System.err.println("Error: exception in TCP thread for " + origin + ": " + socket.getLocalPort() + " " + e.getMessage());
                break;
            }
        }

        try {
            if (! beenStopped()) {
                connectionManager.connectionClosing(connectionId);
            }
        } catch (IOException e) {
            System.err.println("Error closing connection with " + origin);
        }

    }
}
