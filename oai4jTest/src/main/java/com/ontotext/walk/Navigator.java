package com.ontotext.walk;

/**
 * Created by Simo on 14-1-30.
 */
public interface Navigator<T> {
    public void check(T t);
    public boolean shouldStop();
}
