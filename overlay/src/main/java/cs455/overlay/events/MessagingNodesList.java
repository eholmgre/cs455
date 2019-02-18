package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessagingNodesList implements Event{

    private int numNodes;
    private String nodeList;
    private String origin;
    private int connectionId;

    public MessagingNodesList(int numNodes, String nodeList, String origin, int connectionId) {
        this.numNodes = numNodes;
        this.nodeList = nodeList;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public String getNodeList() {
        return nodeList;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public MessageTypes getType() {
        return MessageTypes.MESSAGING_NODES_LIST;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeInt(numNodes);

        byte []nodeListBytes = nodeList.getBytes();
        dOutStream.writeInt(nodeListBytes.length);
        dOutStream.write(nodeListBytes);

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
