package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event {
    private String ip;
    private int port;

    private int numSent;
    private int numRcvd;
    private int numRlyd;

    private long totalSent;
    private long totalRcvd;

    private String origin;
    private int connectionId;

    public TrafficSummary(String ip, int port, int numSent, int numRcvd, int numRlyd, long totalSent, long totalRcvd, String origin, int connectionId) {
        this.ip = ip;
        this.port = port;
        this.numSent = numSent;
        this.numRcvd = numRcvd;
        this.numRlyd = numRlyd;
        this.totalSent = totalSent;
        this.totalRcvd = totalRcvd;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getNumSent() {
        return numSent;
    }

    public int getNumRcvd() {
        return numRcvd;
    }

    public int getNumRlyd() {
        return numRlyd;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public long getTotalRcvd() {
        return totalRcvd;
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
        return MessageTypes.TRAFFIC_SUMMARY;
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

        dOutStream.writeInt(numSent);
        dOutStream.writeInt(numRcvd);
        dOutStream.writeInt(numRlyd);

        dOutStream.writeLong(totalSent);
        dOutStream.writeLong(totalRcvd);

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;
    }
}
