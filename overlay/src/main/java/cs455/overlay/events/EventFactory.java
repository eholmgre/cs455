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

    private RegisterRequest buildRegisterRequest(DataInputStream messageDataStream, String origin) throws IOException {
        int ipLength = messageDataStream.readInt();
        byte []ipBytes = new byte[ipLength];
        messageDataStream.readFully(ipBytes);
        String ipString = new String(ipBytes);

        int port = messageDataStream.readInt();

        return new RegisterRequest(ipString, port, origin);
    }

    private RegisterResponse buildRegisterResponse(DataInputStream messageDataStrean, String origin) throws IOException {
        boolean success = messageDataStrean.readBoolean();

        int infoLength = messageDataStrean.readInt();
        byte []infoBytes = new byte[infoLength];
        messageDataStrean.readFully(infoBytes);
        String info = new String(infoBytes);

        int registerCount = messageDataStrean.readInt();

        return new RegisterResponse(success, info, registerCount, origin);
    }

    public Event createEvent(byte[] msg, String origin) throws IOException {
        Event message;

        ByteArrayInputStream messageByteStream = new ByteArrayInputStream(msg);
        DataInputStream messageDataStream = new DataInputStream(new BufferedInputStream(messageByteStream));

        MessageTypes type = MessageTypes.getTypeEnum(messageDataStream.readInt());

        switch (type) {
            case REGISTER_REQUEST:
                message = buildRegisterRequest(messageDataStream, origin);
                System.out.println("Event factory creating REGISTER_REQUEST");
                break;

            case REGISTER_RESPONSE:
                System.out.println("Event factory creating REGISTER_RESPONSE");
                message = buildRegisterResponse(messageDataStream, origin);
                break;
            default:
                throw new IOException("unknown message enum - how did you even manage that?");

        }

        messageDataStream.close();
        messageByteStream.close();

        return message;
    }
}
