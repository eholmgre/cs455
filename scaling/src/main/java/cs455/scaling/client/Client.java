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

    private SocketChannel client;
    private ByteBuffer buffer;

    private final LinkedList<String> hashes;

    public Client(String []args) {
        pargs = args;
        hashes = new LinkedList<>();
    }

    public void printUsage() {
        System.out.println("Scaling Client Usage:\n\tclient <server host> <server port> <message rate>");
    }

    public void start() {
        // java cs455.scaling.client.Client server-host server-port message-rate

        if (pargs.length != 3) {
            printUsage();
            return;
        }

        InetSocketAddress serverAddr = null;
        int messageRate = -1;

        boolean parseSuccess = true;
        try {
            int serverPort = -1;
            serverPort = Integer.parseInt(pargs[1]);
            messageRate = Integer.parseInt(pargs[2]);

            serverAddr = new InetSocketAddress(pargs[0], serverPort);

        } catch (NumberFormatException e) {
            parseSuccess = false;
        } finally {
            // lol i dont know what this does but it seems to work
            if (serverAddr == null || serverAddr.isUnresolved()) {
                System.out.println("Invalid server host/port " + serverAddr);
                parseSuccess = false;
            }

            if (messageRate < 1) {
                System.out.println("Invalid message rate \"" + pargs[2] + "\"");
                parseSuccess = false;
            }

            if (! parseSuccess) {
                printUsage();
                return;
            }
        }

        Selector selector;

        final int rate = messageRate;

        Thread senderThread = new Thread(() -> {
            try {
                while (! Thread.currentThread().isInterrupted()) {
                    byte []message = MessageMaker.createMessage();
                    buffer = ByteBuffer.wrap(message);

                    synchronized (hashes) {
                        hashes.add(Hasher.SHA1FromBytes(message));
                    }

                    //System.out.println("Sending: " + buffer.array());

                    client.write(buffer);
                    buffer.clear();

                    Thread.sleep(1000 / rate);
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
            client = SocketChannel.open(serverAddr);
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("client connected: " + client.isConnected());
            buffer = ByteBuffer.allocate(256);

            senderThread.start();


            while (true) {
                System.out.println("Selected " + selector.select() + " keys");

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
        SocketChannel server = (SocketChannel) key.channel();

        server.read(buffer);

        String resp = new String(buffer.array()).trim();
        buffer.clear();

        boolean found = false;

        synchronized (hashes) {
            Iterator<String> iter = hashes.iterator();

            while (iter.hasNext()) {
                String hash = iter.next();

                if (hash.equals(resp)) {
                    iter.remove();
                    found = true;
                    break;
                }
            }

            if (! found) {
                System.out.printf("got response not in hash list: [" + resp + "]");
            }
        }
    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
