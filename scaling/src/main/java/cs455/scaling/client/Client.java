package cs455.scaling.client;

import cs455.scaling.util.Hasher;
import cs455.scaling.util.MessageMaker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class Client {

    private String []pargs;

    private SocketChannel server;

    private final LinkedList<String> hashes;

    private ClientStats stats;

    public Client(String []args) {
        pargs = args;
        hashes = new LinkedList<>();
        stats = new ClientStats();
    }

    public void printUsage() {
        System.out.println("Scaling Client Usage:\n\tserver <server host> <server port> <message rate>");
    }

    public void start() {

        if (pargs.length != 3) {
            printUsage();
            return;
        }

        InetSocketAddress serverAddr = null;
        double messageRate = -1;

        boolean parseSuccess = true;
        try {
            int serverPort = -1;
            serverPort = Integer.parseInt(pargs[1]);
            messageRate = Double.parseDouble(pargs[2]);

            serverAddr = new InetSocketAddress(pargs[0], serverPort);

        } catch (NumberFormatException e) {
            parseSuccess = false;
        } finally {
            // lol i dont know what this does but it seems to work
            if (serverAddr == null || serverAddr.isUnresolved()) {
                System.out.println("Invalid server host/port " + serverAddr);
                parseSuccess = false;
            }

            if (messageRate <= 0.0) {
                System.out.println("Invalid message rate \"" + pargs[2] + "\"");
                parseSuccess = false;
            }

            if (! parseSuccess) {
                printUsage();
                return;
            }
        }

        Selector selector;


        final double rate = messageRate;

        Thread senderThread = new Thread(() -> {
            ByteBuffer buffer;
            try {
                while (! Thread.currentThread().isInterrupted()) {
                    byte []message = MessageMaker.createMessage();
                    buffer = ByteBuffer.wrap(message);

                    String hash = Hasher.SHA1FromBytes(message);

                    //System.out.println("Sending message with hash [" + hash + "]");

                    synchronized (hashes) {
                        hashes.add(hash);
                    }

                    synchronized (server) {
                        server.write(buffer);
                    }
                    buffer.clear();

                    stats.incSent();

                    Thread.sleep((long) (1000.0 / rate));
                }
            } catch (IOException e) {
                System.out.println("Error in sender thread: " + e.getMessage());
                stats.stop();
            } catch (InterruptedException e) {
                System.out.println("Sender thread interrupted");
                Thread.currentThread().interrupt();
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Hashing Error: " + e.getMessage());
            }
        });

        try {
            selector = Selector.open();
            System.out.println("Opening connection");
            server = SocketChannel.open(serverAddr);
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_READ);
//            server.connect(serverAddr);
//            System.out.println("server connected: " + server.isConnected());
            boolean connected = false;
            for (int i = 0; i < 30; ++i) {
                if (server.isConnected()) {
                    connected = true;
                    break;
                }

                System.out.print('.');
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getStackTrace());
                }
            }

            if (! connected) {
                System.out.println("Couldn't connect to server in 30 seconds.");
                return;
            }

            System.out.println("Connected to server");

            senderThread.start();
            stats.start();


            while (true) {
                int selected = selector.select(100);

                //if (selected != 0) {
                //    System.out.println("Selected " + selected + " keys.");
                //}

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();


                    if (! key.isValid()) {
                        System.out.println("got invalid key");
                        continue;
                    }

                    if (key.isReadable()) {
                        handleReadable(key);
                    }

                    iter.remove();
                }


            }

        } catch (IOException e) {
            System.out.println("Error in message loop: " + e.getMessage());
            stats.stop();
        }

    }

    private void handleReadable(SelectionKey key) throws IOException { //todo: delegate this to separate thread?
        ByteBuffer buffer = ByteBuffer.allocate(40);
        SocketChannel server = (SocketChannel) key.channel();

        int bytesRead;

        synchronized (server) {
            bytesRead = server.read(buffer);
        }

        byte []message = new byte[bytesRead];

        buffer.flip();
        buffer.get(message);

        stats.incRcvd(); // should i count messages that can't be matched?

        String resp = new String(message);

        boolean found = false;

        synchronized (hashes) {
            Iterator<String> iter = hashes.iterator();

            while (iter.hasNext()) {
                String hash = iter.next();

                if (hash.equals(resp)) {
                    iter.remove();
                    found = true;
                    //System.out.println("removed hash from linked list");
                    break;
                }
            }

            if (! found) {
                System.out.println("got response not in hash list: [" + resp + "]");
            }
        }
    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
