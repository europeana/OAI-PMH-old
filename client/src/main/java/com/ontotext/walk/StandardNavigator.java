package com.ontotext.walk;

import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-18.
 */
public class StandardNavigator implements Navigator<RecordsList> {
    private volatile boolean stop = false;
    public void check(RecordsList recordsList) {

    }

    public boolean shouldStop() {
        return stop;
    }

    public void stop() {
        stop = true;
    }
}
