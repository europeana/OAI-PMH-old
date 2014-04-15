package com.ontotext.walk;

import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-18.
 */
public class StandardNavigator implements Navigator<RecordsList> {
    public void check(RecordsList recordsList) {

    }

    public boolean shouldStop() {
        return false;
    }
}
