package cs455.scaling.server;

import cs455.scaling.util.Hasher;
// import cs455.scaling.util.MessageMaker;

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

    private Selector selector;

    public Server(String []args) {
        pargs = args;
    }

    public void printUsage() {
        System.out.println("Scaling Server Usage:\n\tserver <portnum> <thread pool size> <batch size> <batch time>");
    }

    private void handleAcceptation(SelectionKey key) {
        pool.add(() -> {
            try {
                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                SocketChannel client = server.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                System.out.println("Client registered");
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("why is this happening");
            }
        });
    }

    private void handleMessage(SelectionKey k) {
        pool.add(() -> {
            try {
                //ByteBuffer buffer = ByteBuffer.allocate(9 * 1024); // better safe than sorry
                ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
                SocketChannel client = (SocketChannel) k.channel();

                int bytesRead = 0;

                synchronized (client) {
                    bytesRead = client.read(buffer);
                }

                buffer.flip(); // is this necessary?

                if (bytesRead == -1) {
                    client.close();
                    System.out.println("Client unregistered");
                } else {

                    byte []message = buffer.array();
                    //System.out.println("received [" + new String(message) + "]");

                    String hash = Hasher.SHA1FromBytes(message);

                    //todo: actually figure out the problem
                    if (hash.equals("e1634a16621e3c08ffa8b1379c241fe04cdae284") || hash.equals("0631457264ff7f8d5fb1edc2c0211992a67c73e6")) {
                        System.out.println("aww hell");
                        return;
                    }

                    System.out.println("received message with hash [" + hash + "]");

                    //byte []ret = MessageMaker.readableMessage2();

                    pool.add(() -> {
                        // should i be synchronizing on k?
                        k.interestOps(SelectionKey.OP_WRITE);

                        byte []reply = hash.getBytes();
                        //byte []reply = ret;
                        System.out.println("sending  [" + new String(reply) + "]");

                        ByteBuffer sendBuf = ByteBuffer.wrap(reply);

                        try {

                            synchronized (client) {
                                client.write(sendBuf);
                            }
                        } catch (IOException e) {
                            System.out.println("IOException when sending reply: " + e.getMessage());
                        }

                        k.interestOps(SelectionKey.OP_READ);
                        selector.wakeup(); // might not be necessary w/ select timeout
                    });


                    //System.out.println("Rcvd message, sending [" + hash + "] back to client.");

                    /*

                    buffer.clear();
                    //buffer = ByteBuffer.wrap(hash.getBytes());
                    buffer = ByteBuffer.wrap(ret);
                    client.write(buffer);
                    buffer.clear();

                    */
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
        double batchTime = -1;

        if (pargs.length != 4) {
            printUsage();
            return;
        }

        boolean parseSuccess = true;
        try {
            portNum = Integer.parseInt(pargs[0]);
            numThreads = Integer.parseInt(pargs[1]);
            batchSize = Integer.parseInt(pargs[2]);
            batchTime = Double.parseDouble(pargs[3]);
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

            if (batchTime <= 0.0) {
                System.out.println("Invalid batch time \"" + pargs[3] + "\"");
                parseSuccess = false;
            }
        }

        if (! parseSuccess) {
            printUsage();
            return;
        }

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

        pool = new ThreadPool(numThreads, batchSize, batchTime);
        pool.start();


        // NIO loop

        while (true) {
            try {
                //System.out.println("blocking on select.");
                int selected = selector.select(100);

                if (selected == 0) {
                    continue;
                }

                //if (selected != 0) {
                //    System.out.println("Selected " + selected + " keys.");
                //}

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
                        handleAcceptation(key);
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
