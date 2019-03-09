package cs455.scaling.server;

import cs455.scaling.client.Client;

public class Server {

    String []pargs;
    ThreadPool pool;

    public Server(String []args) {
        pargs = args;
    }

    public void start() {

    }


    public static void main(String []args) {
        Server server = new Server(args);

        server.start();
    }
}
