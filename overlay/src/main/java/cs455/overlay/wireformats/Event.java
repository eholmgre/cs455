package cs455.overlay.wireformats;

public interface Event {
    MessageTypes getType();

    byte[] getBytes();
}
