package cs455.overlay.node;

import cs455.overlay.events.RegisterRequest;
import cs455.overlay.transport.ConnectionManager;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.events.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Registry implements Node {

    protected class MessagingNodeHandle {
        String host;
        int port;
        ArrayList<MessagingNodeHandle> connections;
    }

    protected class RegistryHelperThread implements Runnable {

        private volatile boolean stopped;

        private synchronized boolean beenStopped() {
            return stopped;
        }

        public synchronized void stop() {
            stopped = true;
        }

        private synchronized Registry.RegistryState getState() {
            return registryState;
        }

        public RegistryHelperThread() {
            stopped = false;
        }

        @Override
        public void run() {
            while (!beenStopped()) {
                if (! eventQueue.isEmpty()) {
                    Event e = eventQueue.poll();

                    switch (e.getType()) {
                        case REGISTER_REQUEST:
                            handleRegisterRequest((RegisterRequest) e);
                    }
                }

            }

        }
    }

    private enum RegistryState {
        REGISTRATION, CREATE_OVERLAY, TASK_STARTED, PULL_TRAFFIC, PRINT_STATS
    }

    private String[] progArgs;

    private RegistryState registryState;

    private ArrayList<MessagingNodeHandle> messagingNodes;

    private TCPServerThread tcpServerThread;

    private RegistryHelperThread registryHelper;

    private ConnectionManager connectionManager;

    private ConcurrentLinkedQueue<Event> eventQueue;

    public Registry(String[] args) {
        progArgs = args;
        registryState = RegistryState.REGISTRATION;
        messagingNodes = new ArrayList<>();
        connectionManager = new ConnectionManager();
        eventQueue = new ConcurrentLinkedQueue<>();


    }

    private void handleRegisterRequest(RegisterRequest e) {

    }


    @Override
    public void onEvent(Event event) {
        eventQueue.add(event);
        System.out.println("Registry received event:" + event);

    }

    private void printUsage() {
        System.out.println("Usage: registry <tcp port>");
    }

    public void startRegistry() {
        if (progArgs.length != 1) {
            printUsage();
            System.exit(1);
        }


        try {
            tcpServerThread = new TCPServerThread(Integer.parseInt(progArgs[0]), connectionManager);
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error starting TCP listener on port " + progArgs[0]);
        }
    }

    public static void main(String[] args) {
        Registry registry = new Registry(args);
        registry.startRegistry();
    }
}
