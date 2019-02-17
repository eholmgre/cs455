package cs455.overlay.util;

import cs455.overlay.node.Node;

import javax.print.attribute.standard.NumberUp;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

public class Overlay {

    private ArrayList<MessageNode> nodes;

    private class MessageNode {
        String id;
        String ip;
        int port;
        int connectionId;
        ArrayList<String> connections;

        public MessageNode(String id, String ip, int port, int connectionId) {
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.connectionId = connectionId;
            connections = new ArrayList<>();
        }
    }

    public Overlay() {
        nodes = new ArrayList<>();
    }

    public int getCount() {
        return nodes.size();
    }

    public void addNode(String ip, int port, int connectionId) {
        String id = ip + ":" + port;

        nodes.add(new MessageNode(id, ip, port, connectionId));
    }

    public void removeNode(String id) throws NoSuchElementException {
        if (!nodes.removeIf(n -> n.id.equals(id))) {
            throw new NoSuchElementException("remove: " + id + " not found in overlay");
        }
    }

    public String getIp(String id) throws NoSuchElementException {
        for (MessageNode node : nodes) {
            if (node.id.equals(id)) {
                return node.ip;
            }
        }

        throw new NoSuchElementException("getIp: " + id + " not found in overlay");
    }

    public boolean inNodes(String id) {
        for (MessageNode node : nodes) {
            if (node.id.equals(id)) {
                return true;
            }
        }

        return false;
    }

    private void clearConnections() {
        for (MessageNode node : nodes) {
            node.connections = new ArrayList<>();
        }
    }

    public ArrayList<String[]> generateOverlay(int connectionRequirement) { // what a dumb thing to return
        if (nodes.size() < connectionRequirement + 1 || (nodes.size() * connectionRequirement) % 2 == 1) {
            throw new IllegalArgumentException("Invalid connection requirement.");
        }

        clearConnections();

        ArrayList<String[]> connections = new ArrayList<>();

        int k = connectionRequirement;
        int n = nodes.size();

        for (int i = 0; i < n; i++) {
            for (int m = 1; m < (k / 2) + 1; ++m) {
                int j = (i + m) % n;
                nodes.get(i).connections.add(nodes.get(j).id);
                nodes.get(j).connections.add(nodes.get(i).id);

                connections.add(new String[] {nodes.get(i).id, nodes.get(j).id});
            }

            if (k % 2 == 1 && i < n / 2) {
                int j = (i + (n / 2)) % n;

                nodes.get(i).connections.add(nodes.get(j).id);
                nodes.get(j).connections.add(nodes.get(i).id);

                connections.add(new String[] {nodes.get(i).id, nodes.get(j).id});
            }
        }


        return connections;
    }

    public void generateWeights() {

    }
}
