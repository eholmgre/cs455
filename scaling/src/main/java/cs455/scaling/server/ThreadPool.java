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
        private int batchTime;

        private Timer timer;

        private Batch current;

        private TimerTask doIt = new TimerTask() {
            @Override
            public void run() {
                if (current == null) {
                    return;
                }

                if (taskQueue.offer(current)) {
                    current = null;
                } else {
                    System.out.println("could not add baked batch to queue");
                }
            }
        };

        public Batcher(int size, int time) {
            batchSize = size;
            batchTime = time;
            timer = new Timer();
            // is it bad i start this in ctor?
            timer.scheduleAtFixedRate(doIt, 0, batchTime * 1000);
        }

        public void add(Task t) {
            if (current == null) {
                current = new Batch();
            }

            current.add(t);

            if (current.size() >= batchSize) {
                if (taskQueue.offer(current)) {
                    current = null;
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


    public ThreadPool(int numThreads, int batchSize, int batchTime) {
        this.numThreads = numThreads;
        threads = new LinkedList<>();
        taskQueue = new LinkedBlockingQueue<Batch>();
        batcher = new Batcher(batchSize, batchTime);
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

                    while (b.hasTask()) {
                        Task t = b.next();
                        if (t == null) {
                            System.out.println("what");
                        } else {
                            t.run();
                        }
                    }

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
            threads.add(new Thread(new WorkerThread(i)));
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
