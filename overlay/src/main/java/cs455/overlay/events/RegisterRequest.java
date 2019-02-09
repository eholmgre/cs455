package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterRequest implements Event {

    // IP field contained in request
    private String ip;

    // Port field contained in request
    private int port;

    public RegisterRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public MessageTypes getType() {
        return MessageTypes.REGISTER_REQUEST;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(MessageTypes.REGISTER_REQUEST.getTypeCode());

        byte[] ipBytes = ip.getBytes(); // create byte array for IP string
        dOutStream.writeInt(ipBytes.length);    // write length of IP string
        dOutStream.write(ipBytes);  // write IP string

        dOutStream.writeInt(port);

        dOutStream.flush();
        marshaledBytes = bOutStream.toByteArray();

        bOutStream.close();
        dOutStream.close();

        return marshaledBytes;
    }
}
