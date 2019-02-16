package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterResponse implements Event{
    private String origin;
    private int numberRegistered;
    private boolean success;
    private String info;
    private int connectionId;

    public DeregisterResponse(boolean success, int numberRegistered, String info, String origin, int connectionId) {
        this.success = success;
        this.numberRegistered = numberRegistered;
        this.info = info;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public boolean getSuccess() {
        return success;
    }

    public int getNumberRegistered() {
        return numberRegistered;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public int getConnectionId() {
        return connectionId;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    @Override
    public MessageTypes getType() {
        return MessageTypes.DEREGISTER_RESPONSE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeBoolean(success);

        dOutStream.writeInt(numberRegistered);

        byte []infoBytes = info.getBytes();
        dOutStream.writeInt(infoBytes.length);
        dOutStream.write(infoBytes);

        dOutStream.flush();
        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
