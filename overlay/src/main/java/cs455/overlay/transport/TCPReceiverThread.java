package cs455.overlay.transport;

import cs455.overlay.events.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

// will running event processing operations in this thread cause lost performance/ packets ?
public class TCPReceiverThread implements Runnable{

    private Socket socket;
    private DataInputStream din;
    private EventFactory messageFactory;

    private String origin;

    private volatile boolean isStopped; //TODO: see if we can't stop on socket close

    private synchronized boolean beenStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
    }

    public TCPReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
        isStopped = false;
        messageFactory = EventFactory.getInstance();
        origin = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {

        System.out.println("TCP receiver thread starting for " + origin);

        int dataLength;
        while (socket != null && ! socket.isClosed()) { // todo: probably can simplify
            try {
                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);

                messageFactory.createEvent(data, origin);

            } catch (IOException e) { // includes SocketException
                System.err.println(e.getMessage());
                break;
            }
        }

        System.out.println("TCP receiver thread stopping for " + origin);

    }
}
