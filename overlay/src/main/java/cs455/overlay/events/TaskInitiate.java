package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskInitiate implements Event{
    private int numRounds;
    private String origin;
    private int connectionId;

    public TaskInitiate(int numRounds, String origin, int connectionId) {
        this.numRounds = numRounds;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public int getNumRounds() {
        return numRounds;
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
        return MessageTypes.TASK_INITIATE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeInt(numRounds);

        dOutStream.flush();
        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
