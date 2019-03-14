package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerStats {

    private class ClientData {
        private long numRcvd;
        private long numSent;

        public ClientData() {

        }

        public synchronized long getNumRcvd() {
            return numRcvd;
        }

        public synchronized long getNumSent() {
            return numSent;
        }

        public synchronized void sent() {
            ++numSent;
        }

        public synchronized void rcvd() {
            ++numRcvd;
        }

        public synchronized void reset() {
            numRcvd = 0;
            numSent = 0;
        }

    }

    private long totalRcvd;
    private long totalSent;

    private HashSet<SocketChannel> accepted;

    private HashMap<SocketChannel, ClientData> stats;

    private Timer statsTimer;

    public ServerStats() {
        accepted = new HashSet<>();
        stats = new HashMap<>();
    }

    public synchronized void register(SocketChannel s) {
        if (accepted.contains(s)) {
            throw new IllegalArgumentException("Socket Channel is already registered");
        }

        accepted.add(s);
        stats.put(s, new ClientData());
    }

    public synchronized boolean isRegistered(SocketChannel s) {
        return accepted.contains(s);
    }

    public synchronized void deRegister(SocketChannel s) {
        if (!accepted.contains(s)) {
            throw new IllegalArgumentException("Socket Channel is not registered");
        }

        accepted.remove(s);
    }

    public synchronized void incSent(SocketChannel s) {
        if (!accepted.contains(s)) {
            throw new IllegalArgumentException("Socket Channel is not registered");
        }

        ++totalSent;

        stats.get(s).sent();
    }

    public synchronized void incRcvd(SocketChannel s) {
        if (!accepted.contains(s)) {
            throw new IllegalArgumentException("Socket Channel is not registered");
        }

        ++totalRcvd;

        stats.get(s).rcvd();
    }

    // should this be sent or received?
    private synchronized double computeMean() {

        if (accepted.size() == 0) {
            return Double.POSITIVE_INFINITY;
        }

        double total = 0;

        for (SocketChannel c : accepted) {
            total += stats.get(c).getNumSent();
        }

        return total / (double)accepted.size() / 20.; // per second
    }

    private synchronized double computeSTD() {
        // questionably accurate
        if (accepted.size() == 0) {
            return Double.POSITIVE_INFINITY;
        }

        double x = computeMean();

        double sum = 0;

        for (SocketChannel s : accepted) {
            sum += Math.pow(stats.get(s).getNumSent() - x, 2);
        }


        return Math.pow(sum / accepted.size(), 0.5) / 20; // std per sec?
    }

    private synchronized void reset() {
        for (SocketChannel s : accepted) {
            stats.get(s).reset();
        }

        totalRcvd = 0;
        totalSent = 0;
    }

    /*

    [timestamp]  Server  Throughput:  x  messages/s,  Active  Client  Connections:  y,  Mean  Per-client Throughput: p messages/s, Std. Dev. Of Per-client Throughput: q messages/

     */

    public void start() {
        statsTimer = new Timer();

        statsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.print("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                System.out.printf("] Server Throughput: %.2fm/s, Active Clients: %d", totalSent / 20.0, accepted.size());
                System.out.printf(", Mean per client: %.2fm/s, STD per client: %.2fm/s\n", computeMean(), computeSTD());

                reset();
            }
        }, 20 * 1000, 20 * 1000);
    }

    public void stop() {
        statsTimer.cancel();
    }


}
