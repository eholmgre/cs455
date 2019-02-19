package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LinkWeights implements Event{
    private String origin;
    private int connectionId;

    private int numWeights;
    private ArrayList<String []> weights;


    public LinkWeights(int numWeights, ArrayList<String []> weights, String origin, int connectionId) {
        this.numWeights = numWeights;
        this.weights = weights;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public int getNumWeights() {
        return numWeights;
    }

    public ArrayList<String[]> getWeights() {
        return weights;
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
        return MessageTypes.LINK_WEIGHTS;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeInt(numWeights);

        if (numWeights != weights.size()) {
            throw new IllegalArgumentException("Link Weights message: numWeights != weights.size");
        }

        for (String[]weight : weights) {
            // assuming weight has form ["node1:port", "node2:port", weight]
            String linkInfo = weight[0] + " " + weight[1] + " " + weight[2];
            byte []linkInfoBytes = linkInfo.getBytes();
            dOutStream.writeInt(linkInfoBytes.length);
            dOutStream.write(linkInfoBytes);
        }

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
