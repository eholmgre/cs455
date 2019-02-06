package cs455.overlay.events;

import java.io.IOException;

public interface Event {
    MessageTypes getType();

    byte[] getBytes() throws IOException;
}
