package cs455.scaling.server;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    private int numThreads;
    private LinkedList<Thread> threads;
    private LinkedBlockingQueue<Task> taskQueue;

    public ThreadPool(int numThreads) {
        this.numThreads = numThreads;
        threads = new LinkedList<>();
        taskQueue = new LinkedBlockingQueue<Task>();
    }

    public void add(Task t) {
        if (!taskQueue.offer(t)) {
            System.err.println("ThreadPool: Error adding task to queue");
        }
    }

    private class ThreadRunner implements Runnable {
        final private int id;

        public ThreadRunner(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (! Thread.currentThread().isInterrupted()) { // maybe just a while true?
                    Task t = taskQueue.take();

                    t.run();
                }
            } catch (InterruptedException e) {
                System.out.print("Thread " + id + " stopping.");

                // lol what does this even do?
                Thread.currentThread().interrupt();
            }

        }
    }


    public void start() {
        for (int i = 0; i < numThreads; ++i) {
            threads.add(new Thread(new ThreadRunner(i)));
        }

        for (Thread t : threads) {
            t.start();
        }

    }

    public void stop() {
        for (Thread t : threads) {
            t.interrupt();
        }
    }
}
