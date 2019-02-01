package cs455.overlay.events;

public class IncommingConnectionEvent implements Event {
    /* does this even make sense? */


    @Override
    public MessageTypes getType() {
        return MessageTypes.INCOMMING_CONNECTION;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
