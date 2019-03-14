import cs455.scaling.server.ThreadPool;
import cs455.scaling.util.Hasher;
import cs455.scaling.util.MessageMaker;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Tests {

    //@Test
    @Ignore
    public void testThreadPool() {
        ThreadPool tp = new ThreadPool(10, 10, 10, true);

        Random r = new Random();

        Timer t = new Timer();

        //t.schedule(new TimerTask() {
        //    @Override
        //    public void run() {
        //        Assert.fail();
        //    }
        //}, 75 * 1000);

        tp.start();

        // if events cant catch interupted then this should work lol

        for (int i = 0; i < 20; ++i) {

            try {

                final int num = i;
                tp.add(() -> {
                    System.out.println("A " + num);
                    /*
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.println("A " + num + " interrupted");
                    }
                    */
                });
                System.out.println("Added A " + num);

                Thread.sleep(1000);


                tp.add(() -> {
                    System.out.println("B " + num);
                    /*
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println("B " + num + " interrupted");
                    }
                    */
                });
                System.out.println("Added B " + num);

                Thread.sleep(1000);

                tp.add(() -> {
                    System.out.println("C " + num);
                    /*
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println("C " + num + " interrupted");
                    }
                    */
                });
                System.out.println("Added C " + num);


                Thread.sleep(r.nextInt(5000));


            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }

        }

        System.out.println("trynna stop");

        tp.stop();

        t.cancel();

        System.out.println("done.");
    }


    @Test
    public void testMessageMaker() {
        byte[] message = MessageMaker.createMessage();

        Assert.assertEquals(message.length, 1024 * 8);

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < message.length; ++i) {
            hexString.append(String.format("%02X ", message[i]));
            if (i != 0 && i % 40 == 0) {
                hexString.append("\n");
            }
        }

        System.out.println(hexString.toString());
    }

    @Test
    public void testHasher() {
        Random rand = new Random();
        for (int i = 0; i < 10; ++i) {
            byte[] message = new byte[rand.nextInt(1024)];

            rand.nextBytes(message);

            String hash = "";
            try {
                hash = Hasher.SHA1FromBytes(message);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("What");
            }

            Assert.assertEquals(40, hash.length());

            System.out.println(hash);
        }
    }
}
