package com.ontotext.walk;

/**
 * Created by Simo on 14-1-30.
 */
public interface Navigator<T> {
    void check(T t);
    boolean shouldStop();
    void stop();
}
