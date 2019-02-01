package cs455.overlay.events;

public interface Event {
    MessageTypes getType();

    byte[] getBytes();
}
