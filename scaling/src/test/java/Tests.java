import cs455.scaling.server.Task;
import cs455.scaling.server.ThreadPool;
import org.junit.Test;

import java.util.Random;

public class Tests {

    @Test
    public void testThreadPool() {
        ThreadPool tp = new ThreadPool(1, 5,2);

        Random r = new Random();

        tp.start();

        for (int i = 0; i < 10; ++i) {

            try {

                final int num = i;
                tp.add(new Task() {
                    @Override
                    public void run() {
                        System.out.println("A " + num);
                    }
                });
                System.out.println("Added A " + num);

                Thread.sleep(100);


                tp.add(new Task() {
                    @Override
                    public void run() {
                        System.out.println("B " + num);
                    }
                });
                System.out.println("Added B " + num);

                Thread.sleep(200);

                tp.add(new Task() {
                    @Override
                    public void run() {
                        System.out.println("C " + num);
                    }
                });
                System.out.println("Added C " + num);


                Thread.sleep(r.nextInt(2000));


            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }

        }
    }
}
