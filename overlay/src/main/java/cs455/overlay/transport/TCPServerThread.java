package cs455.overlay.transport;

import cs455.overlay.util.ServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServerThread implements Runnable{

    ServerSocket serverSocket;

    private /* volitile */ boolean isStoped;

    private synchronized boolean beenStoped() {
        return isStoped;
    }

    public synchronized void stop() {
        this.isStoped = true;
    }

    public TCPServerThread(int port) throws IOException {
        ServerSocketFactory ssf = new ServerSocketFactory(port);
        serverSocket = ssf.makeServerSocket();
    }
    @Override
    public void run() {


        while (! beenStoped()) {

        }

    }
}
