package cs455.overlay.events;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {
    // Comrades, this is _our_ instance!
    private static EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {
        // We must seize the means of EventFactory production!
    }

    private RegisterRequest buildRegisterRequest(DataInputStream messageDataStream) throws IOException {
        int ipLength = messageDataStream.readInt();
        byte []ipBytes = new byte[ipLength];
        messageDataStream.readFully(ipBytes);
        String ipString = new String(ipBytes);
        int port = messageDataStream.readInt();

        return new RegisterRequest(ipString, port);
    }

    public Event createEvent(byte[] msg) throws IOException {
        Event message;

        ByteArrayInputStream messageByteStream = new ByteArrayInputStream(msg);
        DataInputStream messageDataStream = new DataInputStream(new BufferedInputStream(messageByteStream));

        MessageTypes type = MessageTypes.getTypeEnum(messageDataStream.readInt());

        switch (type) {
            case REGISTER_REQUEST:
                message = buildRegisterRequest(messageDataStream);
                break;

            default:
                throw new IOException("unknown message enum - how did you even manage that?");

        }

        messageDataStream.close();
        messageByteStream.close();

        return message;
    }
}
