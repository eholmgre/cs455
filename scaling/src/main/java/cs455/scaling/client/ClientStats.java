package cs455.scaling.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ClientStats {

    private long numSent = 0;
    private long numRcvd = 0;

    private Timer statsTimer;

    public synchronized void incSent() {
        ++numSent;
    }

    public synchronized void incRcvd() {
        ++numRcvd;
    }

    private synchronized long getNumSent() {
        return numSent;
    }

    private synchronized long getNumRcvd() {
        return numRcvd;
    }

    private synchronized void reset() {
        numRcvd = 0;
        numSent = 0;
    }

    public void start() {
        statsTimer = new Timer();

        statsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.print("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] ");
                System.out.println( getNumSent() + " messages sent, "
                + getNumRcvd() + " received in last 20 seconds");

                reset();
            }
        }, 20 * 1000, 20 * 1000);
    }

    public void stop() {
        if (statsTimer != null) {
            statsTimer.cancel();
        }
    }


}
