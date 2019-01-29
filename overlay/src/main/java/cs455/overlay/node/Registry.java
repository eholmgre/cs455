package cs455.overlay.node;

import cs455.overlay.util.ServerSocketFactory;
import cs455.overlay.wireformats.Event;

import java.net.Socket;
import java.util.ArrayList;

public class Registry implements Node {

    protected class MessagingNodeHandle {
        String host;
        int port;
        Socket socket;
        ArrayList<MessagingNodeHandle> connections;
    }

    protected class RegistryHelper implements Runnable {
        @Override
        public void run() {

        }
    }

    ArrayList<MessagingNodeHandle> messagingNodes;
    ServerSocketFactory serverSocketFactory;

    @Override
    public void onEvent(Event event) {

    }

    private void printUsage() {
        System.out.println("Usage: registry <tcp port>");
    }

    public static void main(String[] args) {

        Registry registry = new Registry();

        if (args.length != 1) {
            registry.printUsage();
            System.exit(1);
        }

        try {
            registry.serverSocketFactory = new ServerSocketFactory(Integer.parseInt(args[0]));
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException catches parseInt and ServerSocketFactory exceptions
            System.out.println("Invalid port " + args[0]);
        }

    }
}
