package cs455.overlay.transport;

import cs455.overlay.util.ServerSocketFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerThread implements Runnable{

    private ServerSocket serverSocket;

    private /* volatile */ boolean isStopped;

    private ConnectionManager connectionManager;

    private synchronized boolean beenStoped() {
        return isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
    }

    public TCPServerThread(int port, ConnectionManager connectionManager) throws IOException {
        this.connectionManager = connectionManager;
        ServerSocketFactory ssf = new ServerSocketFactory(port);
        serverSocket = ssf.makeServerSocket();
    }
    @Override
    public void run() {
        while (! beenStoped()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                TCPReceiverThread receiver = new TCPReceiverThread(clientSocket);
                Thread receiverThread = new Thread(receiver);
                connectionManager.newConnection(clientSocket, receiver, receiverThread);
            } catch (Exception e) {
                System.out.println("Error: TCP server thread failed.\n" + e.getMessage());
                break;
            }

        }

    }
}
