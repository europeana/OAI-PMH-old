package com.ontotext.walk;

/**
 * Created by Simo on 14-2-20.
 */
public interface Resumable extends Runnable {
    boolean isDone();
    boolean isPaused();
    void pause();
    void resume();
    void stop();
}
