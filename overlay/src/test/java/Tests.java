import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;
import cs455.overlay.events.MessageTypes;
import cs455.overlay.events.RegisterRequest;
import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import cs455.overlay.util.Overlay;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class Tests {

    @Test
    public void testRegistration() {
        MessagingNode node = new MessagingNode(new String[] {"localhost", "6666"});
        Registry registry = new Registry(new String[] {"6666"});

        registry.startRegistry();
        node.startMessageNode();
    }

    @Test
    public void testRegisterRequest() {
        try {
            EventFactory messageFactory = EventFactory.getInstance();

            Event registerRequest = new RegisterRequest("test", -1, "localhost", -1);

            byte []marshaledBytes = registerRequest.getBytes();

            Event unmarshaledEvent = messageFactory.createEvent(marshaledBytes, "localhost", -1);

            assertTrue(unmarshaledEvent.getType().equals(MessageTypes.REGISTER_REQUEST));

            RegisterRequest unmarshaledRequest = (RegisterRequest) unmarshaledEvent;

            assertTrue(unmarshaledRequest.getIp().equals("test"));
            assertTrue(unmarshaledRequest.getPort() == -1);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    public void testGenerateOverlay() {

        // random testing, the most rigorous testing there is..

        Random rand = new Random();

        Overlay overlay = new Overlay();

        int n = rand.nextInt(100);

        int k;
        if (n % 2 == 0) {
            k = 1 + rand.nextInt(n - 1);
        } else {
            k = 2 * ( 1 + rand.nextInt((n / 2) - 1));
        }

        assertTrue(n >= k + 1 && (n * k) % 2 == 0);

        for (int i = 0; i < n; i ++) {
            overlay.addNode("node" + i, 0, 0);
        }

        System.out.println("n = " + n + ", k = " + k);

        ArrayList<String []> connections;

        try {

            connections = overlay.generateOverlay(k);

            System.out.println("# connections = " + connections.size());

            assertTrue(connections.size() == n * (k / 2.0));

            for (String [] con : connections) {
                System.out.println(con[0] + " <---> " + con[1]);
            }
        } catch (IllegalArgumentException e) {
            fail();
        }
    }
}
