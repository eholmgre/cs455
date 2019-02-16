package cs455.overlay.transport;

import cs455.overlay.node.Node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerThread implements Runnable{

    private class ServerSocketFactory {

        private int assignedPort;

        public ServerSocketFactory() {
            assignedPort = -1;
        }

        public ServerSocketFactory(int assignedPort) throws IllegalArgumentException {
            if (assignedPort < 1 || assignedPort > 65535) {
                throw new IllegalArgumentException("Invalid port number");
            }
            this.assignedPort = assignedPort;
        }

        /*
        Adapted from https://stackoverflow.com/questions/2675362
         */
        public ServerSocket makeServerSocket() throws IOException {
            if (assignedPort > 0) {
                return new ServerSocket(assignedPort);
            }

            for (int port = 1024; port < 65535; ++port) {
                try {
                    ServerSocket testSocket; // can this be outside the loop? is that safe? TODO: lets find out
                    testSocket = new ServerSocket(port);
                    assignedPort = port;
                    return testSocket;
                } catch (IOException e) {
                    continue; //unnecessary but for clarity
                }
            }

            throw new IOException("No available ports on machine.");
        }

        public int getPort() throws RuntimeException{
            if (assignedPort > 0) {
                return assignedPort;
            }

            throw new RuntimeException("Port has not been selected yet.");
        }

    }

    private ServerSocket serverSocket;

    private /* volatile */ boolean isStopped;

    private ConnectionManager connections;

    private int port;

    private Node parent;

    private synchronized boolean beenStoped() {
        return isStopped;
    }

    public synchronized void stop() throws IOException{
        serverSocket.close();
    }

    public TCPServerThread(int port, ConnectionManager connectionManager, Node parent) throws IOException {
        this.parent = parent;
        this.connections = connectionManager;
        ServerSocketFactory ssf = new ServerSocketFactory(port);
        serverSocket = ssf.makeServerSocket();
        this.port = ssf.getPort();
    }

    public TCPServerThread(ConnectionManager connectionManager, Node parent) throws IOException {
        this.parent = parent;
        this.connections = connectionManager;
        ServerSocketFactory ssf = new ServerSocketFactory();
        serverSocket = ssf.makeServerSocket();
        port = ssf.getPort();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        while (serverSocket != null && ! serverSocket.isClosed()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                TCPReceiverThread receiver = new TCPReceiverThread(clientSocket, parent);
                Thread receiverThread = new Thread(receiver);
                int connectionID = connections.addConnection(clientSocket, receiver, receiverThread);
                receiver.setConnectionId(connectionID);
                receiverThread.start();
                System.out.println("New connection: " + clientSocket.getInetAddress().getHostAddress());
            } catch (Exception e) {
                System.err.println("Error: TCP server thread failed.\n" + e.getMessage());
                break;
            }

        }

        System.out.println("tcp server thread exit");

    }
}
