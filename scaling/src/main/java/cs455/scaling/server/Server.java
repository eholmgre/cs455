package cs455.scaling.server;

import cs455.scaling.client.Client;

public class Server {

    String []pargs;
    ThreadPool pool;

    public Server(String []args) {
        pargs = args;
    }

    public void printUsage() {
        System.out.println("Scaling Server Usage:\n\tserver <portnum> <thread pool size> <batch size> <batch time>");
    }


    public void start() {

        int portNum = -1;
        int numThreads = -1;
        int batchSize = -1;
        int batchTime = -1;

        if (pargs.length != 4) {
            printUsage();
            return;
        }

        boolean parseSuccess = true;
        try {
            portNum = Integer.parseInt(pargs[0]);
            numThreads = Integer.parseInt(pargs[1]);
            batchSize = Integer.parseInt(pargs[2]);
            batchTime = Integer.parseInt(pargs[3]);
        } catch (NumberFormatException e) {
            parseSuccess = false;
        } finally {
            if (portNum < 1025 || portNum > Math.pow(2, 16) ) {
                System.out.println("Invalid port number \"" + pargs[0] + "\"");
                parseSuccess = false;
            }

            if (numThreads < 1) {
                System.out.println("Invalid thread count \"" + pargs[1] + "\"");
                parseSuccess = false;
            }

            if (batchSize < 1) {
                System.out.println("Invalid batch size \"" + pargs[2] + "\"");
                parseSuccess = false;
            }

            if (batchTime < 1) {
                // TODO: what about non integer times, is that allowed?
                System.out.println("Invalid batch time \"" + pargs[3] + "\"");
                parseSuccess = false;
            }

            if (! parseSuccess) {
                printUsage();
                return;
            }
        }

        // NIO setup?

        // threadpool setup / start?

    }


    public static void main(String []args) {
        Server server = new Server(args);

        server.start();
    }
}
