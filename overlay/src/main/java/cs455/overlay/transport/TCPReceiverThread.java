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

    private int connectionId;

    private volatile boolean isStopped; //TODO: see if we can't stop on socket close

    private synchronized boolean beenStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
    }

    public TCPReceiverThread(Socket socket, Node parent) throws IOException {
        this.socket = socket;
        this.parent = parent;
        din = new DataInputStream(socket.getInputStream());
        isStopped = false;
        messageFactory = EventFactory.getInstance();
        origin = socket.getInetAddress().getHostAddress();
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
                System.err.println("Error: exception in TCP thread for " + origin + ": " + e.getMessage());
                break;
            }
        }

    }
}
