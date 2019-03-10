package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private String []pargs;
    private ThreadPool pool;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    public Server(String []args) {
        pargs = args;
    }

    public void printUsage() {
        System.out.println("Scaling Server Usage:\n\tserver <portnum> <thread pool size> <batch size> <batch time>");
    }

    private void handleAcceptation(Selector selector, ServerSocketChannel serverSocket) {

        pool.add(new Task() {
            @Override
            public void run() {
                try {
                    SocketChannel client = serverSocket.accept();
                    //todo client is null?
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("registered a node");
                } catch (IOException e) {
                    System.out.println("damn thing didnt work");
                }
            }
        });
    }

    private void handleMessage(SelectionKey k) {

        pool.add(new Task() {
            @Override
            public void run() {
                try {
                    ByteBuffer buffer = ByteBuffer.allocate(256);

                    SocketChannel client = (SocketChannel) k.channel();

                    int bytesRead = client.read(buffer);

                    if (bytesRead == -1) {
                        client.close();
                        System.out.println("well, bye");
                    } else {

                        buffer.put("Got: ".getBytes());
                        buffer.flip();
                        client.write(buffer);
                        buffer.clear();
                    }

                } catch (IOException e) {
                    System.out.println("couldnt do it");
                }
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

        try {

            selector = Selector.open();

            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", portNum));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            System.out.println("done messed up with the NIO");
            return;
        }

        // threadpool setup / start?

        pool = new ThreadPool(numThreads, batchSize, batchTime);
        pool.start();


        // NIO loop

        while (true) {
            try {
                selector.select();

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while(iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (! key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()){
                        handleAcceptation(selector, serverSocket);
                    }

                    if (key.isReadable()) {
                        handleMessage(key);
                    }

                    iter.remove();
                }

            } catch (IOException e) {
                System.out.println("oh darn");
            }
        }



    }


    public static void main(String []args) {
        Server server = new Server(args);

        server.start();
    }
}
