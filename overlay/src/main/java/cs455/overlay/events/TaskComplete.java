package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event{
    private String ip;
    private int port;

    private String origin;
    private int connectionId;

    public TaskComplete(String ip, int port, String origin, int connectionId) {
        this.ip = ip;
        this.port = port;
        this.origin = origin;
        this.connectionId = connectionId;

    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
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
        return MessageTypes.TASK_COMPLETE;
    }

    @Override
    public byte[] getBytes() throws IOException {

        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        byte []ipBytes = ip.getBytes();

        dOutStream.writeInt(ipBytes.length);
        dOutStream.write(ipBytes);

        dOutStream.writeInt(port);

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
