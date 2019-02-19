package cs455.overlay.routing;

import java.util.*;

public class SubOverlay {
    private class Edge {
        public String toNode;
        public int weight;

        public Edge(String toNode, int weight) {
            this.toNode = toNode;
            this.weight = weight;
        }

    }

    private class Node {
        int connectionId;
        String nodeId;
        boolean connected;
        boolean isMe;

        ArrayList<Edge> connections;

        public Node(String nodeId, int connectionId, boolean connected) {
            this.nodeId = nodeId;
            this.connectionId = connectionId;
            this.connected = connected;
            connections = new ArrayList<>();

            isMe = nodeId.equals(myId);
        }
    }

    private class ShortestPath {
        String destID;
        String pred;
        ArrayList<String> path;
        ArrayList<Integer> weights;
        int dist;

        public ShortestPath(String nodeId) {
            this.destID = nodeId;
            path = new ArrayList<>();
            weights = new ArrayList<>();
            dist = Integer.MAX_VALUE;
            pred = "";
        }
    }

    private ArrayList<Node> nodes;

    private HashMap<String, ShortestPath> shortestPaths;

    private String myId;
    private Random rand;

    public SubOverlay(String myId) {
        this.myId = myId;
        nodes = new ArrayList<>();
        shortestPaths = new HashMap<>();
        rand = new Random();
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

    public void addEdge(String node1, String node2, int weight) throws NoSuchElementException {
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
            for (Edge e : n.connections) {
                System.out.println("\t\t|--> " + e.toNode + " " + e.weight);
            }
        }
    }

    public void computeShortestPaths() {
        class vertex implements Comparable {
            String nodeId;
            int cost;

            public vertex(String nodeId, int cost) {
                this.nodeId = nodeId;
                this.cost = cost;
            }

            @Override
            public int compareTo(Object o) {
                return cost - ((vertex) o).cost;
            }

        }
        HashMap<String, Integer> nodeLookup = new HashMap<>();

        ArrayList<ShortestPath> paths = new ArrayList<>();

        HashMap<String, String> pred = new HashMap<>();
        HashMap<String, Integer> cost = new HashMap<>();

        PriorityQueue<vertex> queue = new PriorityQueue<>();

        for (int i = 0; i < nodes.size(); ++i) {
            nodeLookup.put(nodes.get(i).nodeId, i);
            paths.add(new ShortestPath(nodes.get(i).nodeId));
        }

        for (Node n : nodes) {
            vertex v;
            if (!n.isMe) {
                cost.put(n.nodeId, Integer.MAX_VALUE);
                v = new vertex(n.nodeId, Integer.MAX_VALUE);
            } else {
                v = new vertex(n.nodeId, 0);
                cost.put(n.nodeId, 0);
            }
            queue.add(v);
        }

        HashSet<String> done = new HashSet<>();

        while (done.size() < nodes.size()) {
            vertex cur = queue.poll();
            done.add(cur.nodeId);
            for (Edge e : nodes.get(nodeLookup.get(cur.nodeId)).connections) {
                if (done.contains(e.toNode)) {
                    continue;
                }
                String current_pred = pred.get(e.toNode);
                int current_cost = cost.get(e.toNode);
                int alt = cur.cost + e.weight;
                if (alt < current_cost) {
                    cost.put(e.toNode, alt);
                    pred.put(e.toNode, cur.nodeId);

                    queue.add(new vertex(e.toNode, alt));
                }
            }
        }

        for (ShortestPath p : paths) {
            if (p.destID.equals(myId)) {
                continue;
            }

            String prev = p.destID;

            while (! prev.equals(myId)){
                p.path.add(0, prev);
                prev = pred.get(prev);
            }

            // p.path.add(0, myId);

            p.dist = cost.get(p.destID);

            System.out.print(p.destID + ": ");
            for (String s : p.path) {
                System.out.print(s + " --> ");
            }


            System.out.println(p.destID + " = " + p.dist);

            shortestPaths.put(p.destID, p);
        }

    }

    public void printShortestPaths() {
        for (Node n : nodes) {
            if (n.isMe) {
                continue;
            }
            String path = "";
            for (String step : shortestPaths.get(n.nodeId).path) {
                path += step;
                //todo: figure this out when youre not dead
                int weight = -1;
                path += "--" + weight + "--";
            }

            path += n.nodeId;

            System.out.println(path);
        }
    }

    public ArrayList<String> getShortestPath(String nodeId) {
        return shortestPaths.get(nodeId).path;
    }

    public int getCost(String nodeId) {
        return shortestPaths.get(nodeId).dist;
    }

    public String getRandomNode() {
        int i = rand.nextInt(nodes.size());

        if (nodes.get(i).isMe) {
            i = (i + 1) % nodes.size();
        }

        return nodes.get(i).nodeId;
    }
}
