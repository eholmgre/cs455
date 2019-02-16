package cs455.overlay.events;

import java.io.IOException;

public interface Event {

    String getOrigin();

    int getConnectionId();

    MessageTypes getType();

    byte[] getBytes() throws IOException;
}
