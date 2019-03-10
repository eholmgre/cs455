package cs455.scaling.server;

import java.util.Timer;

public class Batcher {

    private int batchSize;
    private int batchTime;

    private Timer timer;

    private Batch current;

    private ThreadPool pool;

    public Batcher(int size, int time) {
        batchSize = size;
        batchTime = time;
    }


}
