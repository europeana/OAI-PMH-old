package com.ontotext.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Simo on 2.9.2014 г..
 */
public class WatchDog {
    private static Log log = LogFactory.getLog(WatchDog.class);
    private final int timeoutPeriods;
    private Timer timer = new Timer("WatchDog", true);
    private int intervalsLeft;

    public WatchDog(int timeoutMinutes) {
        this(timeoutMinutes, 60*1000);
    }

    public WatchDog(int timeoutPeriods, long periodLength) {
        this.timeoutPeriods = timeoutPeriods;
        this.intervalsLeft = timeoutPeriods;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (--intervalsLeft == 0) {
                        log.info("WD Exit");
                        System.exit(1);

                    }
                    log.info("WD Continue " + intervalsLeft);
                }
            }
        }, periodLength,  periodLength);
    }

    synchronized public void reset() {
        intervalsLeft = timeoutPeriods;
    }

    public static void main(String[] args) throws InterruptedException {
        final int numIntervals = 5;
        final int intervalLength = 1000;

        final int interval = numIntervals * intervalLength;
        final int offset = 500;
        Random random = new Random();
        WatchDog watchDog = new WatchDog(numIntervals, intervalLength);
        while (true) {
            int sleepTime = random.nextInt(interval + offset);
            log.info("Sleep(" + sleepTime + ")");
            Thread.sleep(sleepTime);
            watchDog.reset();
            log.info("Reset.");
        }
    }
}
