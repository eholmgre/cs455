package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPReceiverThread implements Runnable{

    private Socket socket;
    private DataInputStream din;
    private ConnectionManager connectionManager;

    private /* volitile */ boolean isStopped;

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
    }

    @Override
    public void run() {

        int dataLength;
        while (! beenStopped()) {
            try {
                dataLength = din.readInt();

                byte[] data = new byte[dataLength];
                din.readFully(data, 0, dataLength);

            } catch (IOException e) { // includes SocketException
                System.err.println(e.getMessage());
                break;
            }
        }

    }
}
