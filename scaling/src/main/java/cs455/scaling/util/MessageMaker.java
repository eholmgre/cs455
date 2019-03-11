package cs455.scaling.util;

import java.util.Random;

public class MessageMaker {
    public static byte []createMessage() {
        Random random = new Random(); // slow to use this staticly?
        byte []message = new byte[8 * 1024];
        random.nextBytes(message);

        return message;
    }
}
