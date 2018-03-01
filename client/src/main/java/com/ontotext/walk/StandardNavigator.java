package com.ontotext.walk;

import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-18.
 */
public class StandardNavigator<T> implements Navigator<T> {
    private volatile boolean stop = false;
    public void check(T recordsList) {

    }

    public boolean shouldStop() {
        return stop;
    }

    public void stop() {
        stop = true;
    }
}
