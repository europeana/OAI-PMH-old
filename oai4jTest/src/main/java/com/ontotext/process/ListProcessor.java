package com.ontotext.process;

import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-27.
 */
public interface ListProcessor {
    void processListBegin(RecordsList recordsList);
    void processListEnd(RecordsList recordsList);
    void processListFinish();
    void processListError(Exception e);
}
