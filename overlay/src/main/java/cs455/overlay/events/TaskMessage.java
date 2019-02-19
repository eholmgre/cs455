package cs455.overlay.events;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TaskMessage implements Event{
    //private String dest;
    private ArrayList<String> route;
    private int payload;

    private String origin;
    private int connectionId;

    public TaskMessage(ArrayList<String> route, int payload, String origin, int connectionId) {
        this.route = route;
        this.payload = payload;
        this.origin = origin;
        this.connectionId = connectionId;
    }

    public ArrayList<String> getRoute() {
        return route;
    }

    public int getPayload() {
        return payload;
    }

    public String peelLastHop() {
        return route.remove(0);
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
        return MessageTypes.TASK_MESSAGE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte []marshaledBytes = null;

        ByteArrayOutputStream bOutStream = new ByteArrayOutputStream();
        DataOutputStream dOutStream = new DataOutputStream(new BufferedOutputStream(bOutStream));

        dOutStream.writeInt(getType().getTypeCode());

        dOutStream.writeInt(payload);

        dOutStream.writeInt(route.size());

        for (String hop : route) {
            // assuming weight has form ["node1:port", "node2:port", weight]
            byte []hopBytes = hop.getBytes();
            dOutStream.writeInt(hopBytes.length);
            dOutStream.write(hopBytes);
        }

        dOutStream.flush();

        marshaledBytes = bOutStream.toByteArray();

        dOutStream.close();
        bOutStream.close();

        return marshaledBytes;

    }
}
