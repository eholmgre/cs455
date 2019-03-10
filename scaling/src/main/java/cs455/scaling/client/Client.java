package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    String []pargs;

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
            // lol i dont even know what this really does but it seems to work
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

        try {
            client = SocketChannel.open(serverAddr);
            buffer = ByteBuffer.allocate(256);

            for(int i = 0; i < 100; ++i) {
                buffer = ByteBuffer.wrap(Long.toString(System.currentTimeMillis()).getBytes());
                String respose = null;

                client.write(buffer);
                buffer.clear();
                client.read(buffer);
                respose = new String(buffer.array()).trim();
                buffer.clear();

                System.out.println(respose);
            }
        } catch (IOException e) {
            System.out.println("aww man");
        }
    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
