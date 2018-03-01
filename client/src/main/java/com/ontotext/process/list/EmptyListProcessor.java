package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-11.
 */
public class EmptyListProcessor<T> implements ListProcessor<T> {
    public void processListBegin(T recordsList) {

    }

    public void processListEnd(T recordsList) {

    }

    public void processListFinish() {

    }

    public void processListError(Exception e) {

    }
}
