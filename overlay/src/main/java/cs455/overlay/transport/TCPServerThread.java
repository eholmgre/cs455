package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServerThread implements Runnable{

    ServerSocket serverSocket;

    public TCPServerThread(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    @Override
    public void run() {

    }
}
