package cs455.scaling.client;

public class Client {

    String []pargs;

    public Client(String []args) {
        pargs = args;
    }

    public void start() {

    }

    public static void main(String []args) {
        Client client = new Client(args);

        client.start();
    }
}
