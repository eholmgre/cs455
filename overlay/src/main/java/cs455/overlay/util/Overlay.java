package cs455.overlay.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

public class Overlay {

    private ArrayList<MessageNode> nodes;

    private ArrayList<String []> connections;

    private ArrayList<Integer> weights;

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
        connections = null;
        weights = null;

        String id = ip + ":" + port;

        nodes.add(new MessageNode(id, ip, port, connectionId));
    }

    public void removeNode(String id) throws NoSuchElementException {
        if (!nodes.removeIf(n -> n.id.equals(id))) {
            throw new NoSuchElementException("remove: " + id + " not found in overlay");
        } else {
            connections = null;
            weights = null;
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

    public ArrayList<String []> getNodeInfo() {
        ArrayList<String []> nodeInfo = new ArrayList<>();

        for (MessageNode node : nodes) {
            nodeInfo.add(new String[] {node.ip, Integer.toString(node.port)});
        }

        return nodeInfo;
    }

    public ArrayList<String[]> generateOverlay(int connectionRequirement) { // what a dumb thing to return
        if (nodes.size() < connectionRequirement + 1 || (nodes.size() * connectionRequirement) % 2 == 1) {
            throw new IllegalArgumentException("Invalid connection requirement.");
        }

        ArrayList<MessageNode> nodesShuffle = new ArrayList<>(nodes);
        Collections.shuffle(nodesShuffle);

        weights = null;

        for (MessageNode node : nodesShuffle) {
            node.connections = new ArrayList<>();
        }

        this.connections = new ArrayList<>();

        int k = connectionRequirement;
        int n = nodes.size();

        for (int i = 0; i < n; i++) {
            for (int m = 1; m < (k / 2) + 1; ++m) {
                int j = (i + m) % n;
                nodesShuffle.get(i).connections.add(nodesShuffle.get(j).id);
                nodesShuffle.get(j).connections.add(nodesShuffle.get(i).id);

                connections.add(new String[] {nodesShuffle.get(i).id, nodesShuffle.get(j).id});
            }

            if (k % 2 == 1 && i < n / 2) {
                int j = (i + (n / 2)) % n;

                nodesShuffle.get(i).connections.add(nodesShuffle.get(j).id);
                nodesShuffle.get(j).connections.add(nodesShuffle.get(i).id);

                connections.add(new String[] {nodesShuffle.get(i).id, nodesShuffle.get(j).id});
            }
        }

        return connections;
    }

    public ArrayList<Integer> generateWeights() {
        if (connections == null) {
            System.out.println("you need to generate connections first.");
        }

        weights = new ArrayList<>();

        Random random = new Random();

        for (int i = 0; i < connections.size(); ++i) {
            weights.add(1 + random.nextInt(9));
        }

        return weights;
    }
}
