package cs455.scaling.server;

import cs455.scaling.util.Hasher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private String []pargs;
    private ThreadPool pool;

    public Server(String []args) {
        pargs = args;
    }

    public void printUsage() {
        System.out.println("Scaling Server Usage:\n\tserver <portnum> <thread pool size> <batch size> <batch time>");
    }

    private void handleAcceptation(Selector selector, SocketChannel client) {

        pool.add(() -> {
            try {
                //todo client is null? -- fixed by accepting in nio loop? Is that ok?
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                System.out.println("Client registered");
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
        });
    }

    private void handleMessage(SelectionKey k) {

        pool.add(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(9 * 1024); // better safe than sorry

                SocketChannel client = (SocketChannel) k.channel();

                int bytesRead = client.read(buffer);

                if (bytesRead == -1) {
                    client.close();
                    System.out.println("Client unregistered");
                } else {

                    byte []message = buffer.array();

                    String hash = Hasher.SHA1FromBytes(message);

                    buffer.clear();
                    buffer = ByteBuffer.wrap(hash.getBytes());
                    client.write(buffer);
                    buffer.clear();
                }

            } catch (IOException e) {
                System.out.println("Error handling message: " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Error computing hash: " + e.getMessage());
            }
        });
    }


    public void start() {

        int portNum = -1;
        int numThreads = -1;
        int batchSize = -1;
        int batchTime = -1;

        if (pargs.length != 4) {
            printUsage();
            return;
        }

        boolean parseSuccess = true;
        try {
            portNum = Integer.parseInt(pargs[0]);
            numThreads = Integer.parseInt(pargs[1]);
            batchSize = Integer.parseInt(pargs[2]);
            batchTime = Integer.parseInt(pargs[3]);
        } catch (NumberFormatException e) {
            parseSuccess = false;
        } finally {
            if (portNum < 1025 || portNum > Math.pow(2, 16) ) {
                System.out.println("Invalid port number \"" + pargs[0] + "\"");
                parseSuccess = false;
            }

            if (numThreads < 1) {
                System.out.println("Invalid thread count \"" + pargs[1] + "\"");
                parseSuccess = false;
            }

            if (batchSize < 1) {
                System.out.println("Invalid batch size \"" + pargs[2] + "\"");
                parseSuccess = false;
            }

            if (batchTime < 1) {
                // TODO: what about non integer times, is that allowed?
                System.out.println("Invalid batch time \"" + pargs[3] + "\"");
                parseSuccess = false;
            }

            if (! parseSuccess) {
                printUsage();
                return;
            }
        }

        // NIO setup

        Selector selector;
        ServerSocketChannel serverSocket;

        try {

            selector = Selector.open();

            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", portNum));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            System.out.println("Error setting up NIO: " + e.getMessage());
            return;
        }

        // threadpool setup / start?

        pool = new ThreadPool(numThreads, batchSize, batchTime);
        pool.start();


        // NIO loop

        while (true) {
            try {
                //System.out.println("blocking on select.");
                /* System.out.println("Selected " + */selector.select(100) /* + " keys.")*/;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while(iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (! key.isValid()) {
                        System.out.println("got invalid key");
                        continue;
                    }

                    if (key.isAcceptable()){
                        //System.out.println("got acceptable key");
                        handleAcceptation(selector, serverSocket.accept());
                    }

                    if (key.isReadable()) {
                        //System.out.println("got message key");
                        handleMessage(key);
                    }

                    iter.remove();
                }

            } catch (IOException e) {
                System.out.println("Error in NIO loop: " + e.getMessage());
            }
        }



    }


    public static void main(String []args) {
        Server server = new Server(args);

        server.start();
    }
}
