package cs455.overlay.transport;

import java.io.DataInputStream;
import java.util.ArrayList;

public class ConnectionManager {
    private class Connection {
        public String identifier;
        public String address;
        public int port;
        public DataInputStream in;
        public DataInputStream out;
        public Thread receiverThread;
    }

    private ArrayList<Connection> connections;


    public ConnectionManager() {
        connections = new ArrayList<>();
    }

    /* ACCESS MUST BY SYNCRONIZED */
    public void newConnection() {

    }
}
