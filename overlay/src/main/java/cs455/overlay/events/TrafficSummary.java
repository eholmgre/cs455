package cs455.overlay.events;

import java.io.IOException;

public class TrafficSummary implements Event {
    private String ip;
    private int port;

    private int numSent;
    private int numRcvd;
    private int numRlyd;

    private int totalSent;
    private int totalRcvd;

    private String origin;
    private int connectionId;

    public TrafficSummary(String ip, int port, int numSent, int numRcvd, int numRlyd, int totalSent, int totalRcvd, String origin, int connectionId) {
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
        return new byte[0];
    }
}
