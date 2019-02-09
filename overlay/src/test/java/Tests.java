import cs455.overlay.events.Event;
import cs455.overlay.events.EventFactory;
import cs455.overlay.events.MessageTypes;
import cs455.overlay.events.RegisterRequest;
import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class Tests {

    @Test
    public void testRegisterRequest() {
        try {
            EventFactory messageFactory = EventFactory.getInstance();

            Event registerRequest = new RegisterRequest("test", -1);

            byte []marshaledBytes = registerRequest.getBytes();

            Event unmarshaledEvent = messageFactory.createEvent(marshaledBytes);

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
