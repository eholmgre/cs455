import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;
import cs455.overlay.events.MessageTypes;
import cs455.overlay.events.RegisterRequest;
import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import org.junit.*;

import java.io.IOException;

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

}
