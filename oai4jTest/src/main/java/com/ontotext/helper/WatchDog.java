package com.ontotext.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Simo on 2.9.2014 г..
 */
public class WatchDog {
    private static Log log = LogFactory.getLog(WatchDog.class);
    private final int timeoutPeriods;
    private Timer timer = new Timer("WatchDog", true);
    private int periodsLeft;

    public WatchDog(int timeoutMinutes) {
        this(timeoutMinutes, 60*1000);
    }

    public WatchDog(final int timeoutPeriods, long periodLength) {
        this.timeoutPeriods = timeoutPeriods;
        this.periodsLeft = timeoutPeriods;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    if (--periodsLeft == 0) {
                        log.fatal("Main thread hanged. System.exit(1)");
                        System.exit(1);
                    }
                    final String msg = "Hits left " + periodsLeft;
                    if (periodsLeft *2 < timeoutPeriods) {
                        log.warn(msg);
                    } else {
                        log.debug(msg);
                    }
                }
            }
        }, periodLength,  periodLength);
    }

    synchronized public void reset() {
        periodsLeft = timeoutPeriods;
    }

//    public static void main(String[] args) throws InterruptedException {
//        final int numIntervals = 5;
//        final int intervalLength = 1000;
//        final int numIterations = 5;
//
//        final int interval = numIntervals * intervalLength;
//        final int offset = 500;
//        Random random = new Random();
//        WatchDog watchDog = new WatchDog(numIntervals, intervalLength);
//        for (int i = 0; i != numIterations; ++i) {
//            int sleepTime = random.nextInt(interval + offset);
//            log.info("Sleep(" + sleepTime + ")");
//            Thread.sleep(sleepTime);
//            watchDog.reset();
//            log.info("Reset.");
//        }
//
//        log.info("Normal exit");
//    }
}
