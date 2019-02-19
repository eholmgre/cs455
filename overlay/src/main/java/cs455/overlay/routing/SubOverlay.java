package cs455.overlay.routing;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class SubOverlay {
    private class Edge {
        public String source;
        public int weight;

        public Edge(String source, int weight) {
            this.source = source;
            this.weight = weight;
        }
    }
    private class Node {
        int connectionId;
        String nodeId;
        boolean connected;

        ArrayList<Edge> connections;

        public Node(String nodeId, int connectionId, boolean connected) {
            this.nodeId = nodeId;
            this.connectionId = connectionId;
            this.connected = connected;
            connections = new ArrayList<>();
        }
    }

    private ArrayList<Node> nodes;

    public SubOverlay() {
        nodes = new ArrayList<>();
    }

    public int numConnected() {
        int conns = 0;
        for (Node n : nodes) {
            if (n.connected) {
                ++conns;
            }
        }
        return conns;
    }

    public int size() {
        return nodes.size();
    }

    private Node getNode(String nodeId) throws NoSuchElementException {
        for (Node n : nodes) {
            if (n.nodeId.equals(nodeId)) {
                return n;
            }
        }

        throw new NoSuchElementException();
    }

    public void addConnection(String nodeId, int connectionId) {
        nodes.add(new Node(nodeId, connectionId, true));
    }

    public void addNode(String nodeId) {
        nodes.add(new Node(nodeId, -1, false));
    }

    public void addEdge(String node1, String node2, int weight) throws NoSuchElementException{
        getNode(node1).connections.add(new Edge(node2, weight));
        getNode(node2).connections.add(new Edge(node1, weight));

    }

    public boolean inOverlay(String nodeId) {
        try {
            getNode(nodeId);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }

    }

    public boolean isConnected(String nodeId) throws NoSuchElementException {
        return getNode(nodeId).connected;

    }

    public void printOverlay() {
        for (Node n : nodes) {
            System.out.println("\t" + n.nodeId + " " + n.connected);
            for (Edge e : n.connections){
                System.out.println("\t\t|--> " + e.source + " " + e.weight);
            }
        }
    }
}
