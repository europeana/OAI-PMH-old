package com.ontotext.walk;

/**
 * Created by Simo on 14-2-26.
 */
public class TestWalker implements Resumable {
    private volatile int state = STATE_NEW;


    private static final int STATE_NEW = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_DONE = 3;


    private static class DummyJob {
        private long timeSlept = 0L;

        private static final long SLEEP_TIME = 500L;
        private final long sleepLimit;

        public DummyJob(int totalSeconds) {
            this.sleepLimit = totalSeconds*1000L;
        }

        public boolean doJob() {
            if (timeSlept >= sleepLimit) {
                return true;
            }
            System.out.println("Doing something ... " + timeSlept/100);
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            timeSlept += SLEEP_TIME;
            return timeSlept >= sleepLimit;
        }

    }

    public boolean isDone() {
        return (state == STATE_DONE);
    }

    public boolean isPaused() {
        return (state == STATE_PAUSED);
    }

    public void pause() {
        System.out.println("pause()");
        switch (state) {
            case STATE_DONE:
                break;
            default:
                state = STATE_PAUSED;
                break;
        }
    }

    public void resume() {
        System.out.println("resume()");
        switch (state) {
            case STATE_PAUSED:
                state = STATE_RUNNING;
        }
    }

    public void stop() {
        System.out.println("stop()");
        state = STATE_DONE;
    }

    public void run() {
        System.out.println("run()");
        if (state != STATE_NEW) {
            System.out.println("Invalid sate");
            return;
        }
        state = STATE_RUNNING;
        DummyJob theJob = new DummyJob(10);
        boolean finished = false;
        try {
            do {
                 switch (state) {
                     case STATE_RUNNING:
                         finished = theJob.doJob();
                         break;
                     case STATE_PAUSED:
                         System.out.println("Paused. Sleeping 1 sec ...");
                         Thread.sleep(1000L);
                         break;
                     case STATE_DONE:
                         finished = true;
                         break;
                 }
            } while (!finished);
            state = STATE_DONE;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Exit run()");
    }


    public static void main(String[] args) {
        TestWalker tw = new TestWalker();
        Thread thread = new Thread(tw);
        thread.start();
        try {
            Thread.sleep(3000L);
            System.out.println("Pausing ... ");
            tw.pause();
            Thread.sleep(2000L);
            System.out.println("Resuming ...");
            tw.resume();
            System.out.println("Sleep ....");
            Thread.sleep(4000L);
            System.out.println("Stand up ...");
//            tw.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
