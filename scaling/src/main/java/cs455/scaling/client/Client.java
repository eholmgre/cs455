package cs455.scaling.client;

import java.net.InetSocketAddress;

public class Client {

    String []pargs;

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
    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
