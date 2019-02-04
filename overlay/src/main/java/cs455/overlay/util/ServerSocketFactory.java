package cs455.overlay.util;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketFactory {
    /*
    Erik Holmgren
    2019-01-29

    ServerSocketFactory - my unnecessarily complex way of making network workers more generic

    Either takes no arguments and then selects a free port on which to create the ServerSocket,
        or takes a valid port which is then used to initialize the ServerSocket

    Probably should be a singleton but also probably shouldn't even exist.
     */


    private int assignedPort;

    public ServerSocketFactory() {
        assignedPort = -1;
    }

    public ServerSocketFactory(int assignedPort) throws IllegalArgumentException {
        if (assignedPort < 1 || assignedPort > 65535) {
            throw new IllegalArgumentException("Invalid port number");
        }
        this.assignedPort = assignedPort;
    }

    /*
    Adapted from https://stackoverflow.com/questions/2675362
     */
    public ServerSocket makeServerSocket() throws IOException {
        if (assignedPort > 0) {
            return new ServerSocket(assignedPort);
        }

        for (int port = 1024; port < 65535; ++port) {
            try {
                ServerSocket testSocket; // can this be outside the loop? is that safe? TODO: lets find out
                testSocket = new ServerSocket(port);
                assignedPort = port;
                return testSocket;
            } catch (IOException e) {
                continue; //unnecessary but for clarity
            }
        }

        throw new IOException("No available ports on machine.");
    }

    public int getPort() throws RuntimeException{
        if (assignedPort > 0) {
            return assignedPort;
        }

        throw new RuntimeException("Port has not been selected yet.");
    }

}
