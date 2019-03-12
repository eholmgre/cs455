package cs455.scaling.server;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

    private class Batch {

        private LinkedList<Task> stuffToDo;

        public Batch() {
            stuffToDo = new LinkedList<>();
        }

        public int size() {
            return stuffToDo.size();
        }

        public void add(Task t) {
            stuffToDo.add(t);
        }

        public boolean hasTask() {
            return ! stuffToDo.isEmpty();
        }

        public Task next() {
            return stuffToDo.pop();
        }

    }

    private class Batcher {
        private int batchSize;
        private double batchTime;

        private Timer timer;

        private Batch current;

        public Batcher(int size, double time) {
            batchSize = size;
            batchTime = time;
        }

        public synchronized void add(Task t) {
            if (current == null) {
                if (debug) System.out.println("new batch " + System.currentTimeMillis() / 1000);
                current = new Batch();

                timer = new Timer("Batch Timer");
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (current == null) {
                            return;
                        }

                        if (taskQueue.offer(current)) {
                            if (debug) System.out.println("submitted batch (time) " + System.currentTimeMillis() / 1000);
                            current = null;
                        } else {
                            System.out.println("Could not add batch to queue after batch time");
                        }
                    }
                }, (int) (batchTime * 1000));
            }

            current.add(t);

            if (current.size() >= batchSize) {
                if (taskQueue.offer(current)) {
                    current = null;
                    timer.cancel();
                    if (debug) System.out.println("submitted batch (size) " + System.currentTimeMillis() / 1000);
                } else {
                    System.out.println("Could not full batch to queue");
                }
            }
        }
    }

    private int numThreads;
    private LinkedList<Thread> threads;
    private LinkedBlockingQueue<Batch> taskQueue;

    private Batcher batcher;

    private final boolean debug;


    public ThreadPool(int numThreads, int batchSize, double batchTime) {
        this.numThreads = numThreads;
        threads = new LinkedList<>();
        taskQueue = new LinkedBlockingQueue<>();
        batcher = new Batcher(batchSize, batchTime);
        debug = false;
    }

    public ThreadPool(int numThreads, int batchSize, double batchTime, boolean debug) {
        this.numThreads = numThreads;
        threads = new LinkedList<>();
        taskQueue = new LinkedBlockingQueue<>();
        batcher = new Batcher(batchSize, batchTime);
        this.debug = debug;
    }


    public void add(Task t) {
        batcher.add(t);
    }

    private class WorkerThread implements Runnable {
        final private int id;

        public WorkerThread(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (! Thread.currentThread().isInterrupted()) { // maybe just a while true?
                    Batch b = taskQueue.take();
                    if (debug) System.out.println("thread " + id + " running batch");

                    while (b.hasTask() && ! Thread.currentThread().isInterrupted()) {
                        b.next().run();
                    }

                }
            } catch (InterruptedException e) {
                System.out.println("Thread " + id + " stopping.");

                // lol what does this even do?
                Thread.currentThread().interrupt();
            }
        }
    }


    public void start() {
        for (int i = 0; i < numThreads; ++i) {
            threads.add(new Thread(new WorkerThread(i)));
        }

        for (Thread t : threads) {
            t.start();
        }
    }

    public void stop() {
        for (Thread t : threads) {
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Interupted while joining thread: " + e.getStackTrace());
            }
        }
    }
}
