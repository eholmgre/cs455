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
    //private ByteBuffer buffer;

    private final LinkedList<String> hashes;

    public Client(String []args) {
        pargs = args;
        hashes = new LinkedList<>();
    }

    public void printUsage() {
        System.out.println("Scaling Client Usage:\n\tserver <server host> <server port> <message rate>");
    }

    public void start() {
        // java cs455.scaling.server.Client server-host server-port message-rate

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
                    //byte []message = MessageMaker.readableMessage();
                    buffer = ByteBuffer.wrap(message);

                    String hash = Hasher.SHA1FromBytes(message);

                    System.out.println("Sending message with hash [" + hash + "]");
                    //System.out.println("Sending  [" + new String(message) + "]");

                    synchronized (hashes) {
                        hashes.add(hash);
                    }

                    server.write(buffer);
                    buffer.clear();

                    Thread.sleep((long) (1000.0 / rate));
                }
            } catch (IOException e) {
                System.out.println("Error in sender thread: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Sender thread interrupted");
                Thread.currentThread().interrupt();
            } catch (NoSuchAlgorithmException e) {
                System.out.printf("Hashing Error: " + e.getMessage());
            }
        });

        try {
            selector = Selector.open();
            System.out.println("Opening connection");
            server = SocketChannel.open(serverAddr);
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_READ);
            System.out.println("server connected: " + server.isConnected());
            senderThread.start();


            while (true) {
                int selected = selector.select();

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
        }
    }

    private void handleReadable(SelectionKey key) throws IOException { //todo: delegate this to separate thread?
        ByteBuffer buffer = ByteBuffer.allocate(40);
        SocketChannel server = (SocketChannel) key.channel();

        server.read(buffer);

        buffer.flip();

        String resp = new String(buffer.array());
        //System.out.println("received [" + resp + "]");
        buffer.clear();

        boolean found = false;

        synchronized (hashes) {
            Iterator<String> iter = hashes.iterator();

            while (iter.hasNext()) {
                String hash = iter.next();

                //System.out.println(hash + " vs " + resp);

                if (hash.equals(resp)) {
                    iter.remove();
                    found = true;
                    System.out.println("removed hash from linked list");
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
