package cs455.overlay.events;

/*
Enum of message types cuz that's cool I guess
Strategy of converting int to enum from https://codingexplained.com/coding/java/enum-to-integer-and-integer-to-enum
 */

import java.util.HashMap;

public enum MessageTypes {
    REGISTER_REQUEST (0), REGISTER_RESPONSE (1), DEREGISTER_REQUEST (2), DEREGISTER_RESPONSE (3),
    MESSAGING_NODES_LIST (4), LINK_WEIGHTS(5), TASK_INITIATE (6), TASK_COMPLETE (7),
    PULL_TRAFFIC_SUMMARY (8), TRAFFIC_SUMMARY (9), MESSAGING_NODE_HANDSHAKE (10);

    private int value;

    private static HashMap<Integer, MessageTypes> valueMap = new HashMap<>();

    private MessageTypes(int value) {
        this.value = value;
    }

    static {
        for (MessageTypes m : MessageTypes.values()) {
            valueMap.put(m.value, m);
        }
    }

    public int getTypeCode() {
        return this.value;
    }

    public static MessageTypes getTypeEnum(int code) {
        return valueMap.get(code);
    }
}

