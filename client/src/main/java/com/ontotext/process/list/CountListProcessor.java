package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.IdentifiersList;

public class CountListProcessor implements ListProcessor<IdentifiersList> {
    private static final Log log = LogFactory.getLog(CountListProcessor.class);
    private int count = 0;

    public void processListBegin(IdentifiersList recordsList) {
        count += recordsList.size();
    }

    public void processListEnd(IdentifiersList recordsList) {

    }

    public void processListFinish() {
        log.info("Counted number: " + count);
    }

    public void processListError(Exception e) {

    }
}
