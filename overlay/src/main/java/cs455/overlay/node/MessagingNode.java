package cs455.overlay.node;

import cs455.overlay.events.Event;


public class MessagingNode implements Node{

    private enum NodeState {
        REGISTERING, REGISTERED, DEREGISTERING, CONNECTING, ROUTING, WORKING, TASK_COMPLETE, SENDING_SUMMARY;
    }

    String registryAddress;
    int registryPort;


    public MessagingNode(String address, int port) {

    }

    private static void printHelp() {
        System.out.println("Usage: messageNode <registry address> <registry port>");
    }

    private class MessagingNodeHelper implements Runnable {
        @Override
        public void run() {

        }
    }

    @Override
    public void onEvent(Event event) {

    }

    public static void main(String[] args) {
        if (args.length != 2) {
            MessagingNode.printHelp();
            System.exit(1);
        }


        MessagingNode node = new MessagingNode(args[0], Integer.parseInt(args[1]));


    }
}
