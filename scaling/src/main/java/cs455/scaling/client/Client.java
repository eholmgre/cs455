package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Client {

    private String []pargs;

    private SocketChannel client;
    private ByteBuffer buffer;

    public Client(String []args) {
        pargs = args;
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

        Thread senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (! Thread.currentThread().isInterrupted()) {
                        buffer = ByteBuffer.wrap(Long.toString(System.currentTimeMillis()).getBytes());

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
                }
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
                        //System.out.println("got message key");

                        SocketChannel server = (SocketChannel) key.channel();

                        server.read(buffer);

                        String resp = new String(buffer.array()).trim();
                        buffer.clear();

                        System.out.println("got: " + resp);
                    }

                    iter.remove();
                }


            }

        } catch (IOException e) {
            System.out.println("Error in message loop: " + e.getMessage());
        }
    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
