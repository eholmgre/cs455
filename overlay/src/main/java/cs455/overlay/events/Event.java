package cs455.overlay.events;

import java.io.IOException;

public interface Event {

    String getOrigin();

    MessageTypes getType();

    byte[] getBytes() throws IOException;
}
