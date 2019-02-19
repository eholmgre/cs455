package cs455.overlay.events;

import java.io.*;
import java.util.ArrayList;

public class EventFactory {
    // Comrades, this is _our_ instance!
    private static EventFactory ourInstance = new EventFactory();

    public static EventFactory getInstance() {
        return ourInstance;
    }

    private EventFactory() {
        // We must seize the means of EventFactory production!
    }

    private RegisterRequest buildRegisterRequest(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        int ipLength = messageDataStream.readInt();
        byte []ipBytes = new byte[ipLength];
        messageDataStream.readFully(ipBytes);
        String ipString = new String(ipBytes);

        int port = messageDataStream.readInt();

        return new RegisterRequest(ipString, port, origin, connectionId);
    }

    private RegisterResponse buildRegisterResponse(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        boolean success = messageDataStream.readBoolean();

        int infoLength = messageDataStream.readInt();
        byte []infoBytes = new byte[infoLength];
        messageDataStream.readFully(infoBytes);
        String info = new String(infoBytes);

        int registerCount = messageDataStream.readInt();

        return new RegisterResponse(success, info, registerCount, origin, connectionId);
    }

    private DeregisterRequest buildDeregisterRequest(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        int ipLength = messageDataStream.readInt();
        byte []ipBytes = new byte[ipLength];
        messageDataStream.readFully(ipBytes);
        String ip = new String(ipBytes);

        int port = messageDataStream.readInt();

        return new DeregisterRequest(ip, port, origin, connectionId);
    }

    private DeregisterResponse buildDeregisterResponse(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        boolean success = messageDataStream.readBoolean();

        int numberRegistered = messageDataStream.readInt();

        int infoLength = messageDataStream.readInt();
        byte []infoBytes = new byte[infoLength];
        messageDataStream.readFully(infoBytes);
        String info = new String(infoBytes);

        return new DeregisterResponse(success, numberRegistered, info, origin, connectionId);
    }

    private MessagingNodesList buildMessagingNodesList(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        int numNodes = messageDataStream.readInt();

        int nodeListLength = messageDataStream.readInt();
        byte []nodeListBytes = new byte[nodeListLength];
        messageDataStream.readFully(nodeListBytes);
        String nodeList = new String(nodeListBytes);

        return new MessagingNodesList(numNodes, nodeList, origin, connectionId);
    }

    private MessagingNodeHandshake buildMessagingNodeHandshake(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        int ipLength = messageDataStream.readInt();
        byte []ipBytes = new byte[ipLength];
        messageDataStream.readFully(ipBytes);
        String ipString = new String(ipBytes);

        int port = messageDataStream.readInt();

        return new MessagingNodeHandshake(ipString, port, origin, connectionId);
    }

    private LinkWeights buildLinkWeights(DataInputStream messageDataStream, String origin, int connectionId) throws IOException {
        int numWeights = messageDataStream.readInt();

        ArrayList<String []> weights = new ArrayList<>();

        for (int i = 0; i < numWeights; ++i) {
            int linkInfoSize = messageDataStream.readInt();
            byte []linkInfoBytes = new byte[linkInfoSize];
            messageDataStream.readFully(linkInfoBytes);
            weights.add(new String(linkInfoBytes).split(" "));
        }

        return new LinkWeights(numWeights, weights, origin, connectionId);
    }

    public Event createEvent(byte[] msg, String origin, int connectionId) throws IOException {
        Event message;

        ByteArrayInputStream messageByteStream = new ByteArrayInputStream(msg);
        DataInputStream messageDataStream = new DataInputStream(new BufferedInputStream(messageByteStream));

        MessageTypes type = MessageTypes.getTypeEnum(messageDataStream.readInt());

        switch (type) {
            case REGISTER_REQUEST:
                message = buildRegisterRequest(messageDataStream, origin, connectionId);
                break;

            case REGISTER_RESPONSE:
                message = buildRegisterResponse(messageDataStream, origin, connectionId);
                break;
            case DEREGISTER_REQUEST:
                message = buildDeregisterRequest(messageDataStream, origin, connectionId);
                break;
            case DEREGISTER_RESPONSE:
                message = buildDeregisterResponse(messageDataStream, origin, connectionId);
                break;
            case MESSAGING_NODES_LIST:
                message = buildMessagingNodesList(messageDataStream, origin, connectionId);
                break;
            case MESSAGING_NODE_HANDSHAKE:
                message = buildMessagingNodeHandshake(messageDataStream, origin, connectionId);
                break;
            case LINK_WEIGHTS:
                message = buildLinkWeights(messageDataStream, origin, connectionId);
                break;
            default:
                throw new IOException("unknown message enum - how did you even manage that?");

        }

        messageDataStream.close();
        messageByteStream.close();

        return message;
    }
}
