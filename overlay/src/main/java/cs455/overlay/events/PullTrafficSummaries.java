package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PullTrafficSummaries implements Event {
    private String origin;
    private int connectionId;

    public PullTrafficSummaries(String origin, int connectionId) {
        this.origin = origin;
        this.connectionId = connectionId;
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
        return MessageTypes.PULL_TRAFFIC_SUMMARY;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;

    }
}
