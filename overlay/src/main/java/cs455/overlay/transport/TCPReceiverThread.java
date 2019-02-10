package cs455.overlay.transport;

import cs455.overlay.events.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPReceiverThread implements Runnable{

    private Socket socket;
    private DataInputStream din;
    private EventFactory messageFactory;

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
    }

    @Override
    public void run() {

        int dataLength;
        while (! beenStopped()) {
            try {
                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);

                messageFactory.createEvent(data);

            } catch (IOException e) { // includes SocketException
                System.err.println(e.getMessage());
                break;
            }
        }

    }
}
