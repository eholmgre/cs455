package cs455.overlay.node;

import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.events.Event;

import java.io.IOException;
import java.util.ArrayList;

public class Registry implements Node {

    protected class MessagingNodeHandle {
        String host;
        int port;
        ArrayList<MessagingNodeHandle> connections;
    }

    protected class RegistryHelperThread implements Runnable {

        private synchronized Registry.RegistryState getState() {
            return registryState;
        }
        @Override
        public void run() {
            while (getState() == RegistryState.REGISTRATION) {

            }

        }
    }

    private enum RegistryState {
        REGISTRATION, CREATE_OVERLAY, TASK_STARTED, PULL_TRAFFIC, PRINT_STATS
    }

    private RegistryState registryState;

    private ArrayList<MessagingNodeHandle> messagingNodes;

    private TCPServerThread tcpServerThread;

    private RegistryHelperThread registryHelper;

    private ConnectionManager connectionManager;

    public Registry() {
        registryState = RegistryState.REGISTRATION;
        messagingNodes = new ArrayList<>();

    }

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
            TCPServerThread tcpServerThread = new TCPServerThread(Integer.parseInt(args[0]),
                    registry.connectionManager);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error starting TCP listener on port " + args[0]);
        }

    }
}
