package cs455.overlay.events;

import java.io.*;

public class RegisterResponse implements Event{
    private String origin;

    private boolean success;

    private String info;

    private int registerCount;

    private int connectionId;

    public RegisterResponse(boolean success, String info, int registerCount, String origin, int connectionId) {
        this.success = success;
        this.info = info;
        this.registerCount = registerCount;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getInfo() {
        return info;
    }

    public int getRegisterCount() {
        return registerCount;
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
        return MessageTypes.REGISTER_RESPONSE;
    }

    @Override
    public byte []getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeBoolean(success);

        byte []infoBytes = info.getBytes();
        dOutStream.writeInt(infoBytes.length);
        dOutStream.write(infoBytes);

        dOutStream.writeInt(registerCount);

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
