package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public class TraceListProcessor implements ListProcessor {
    private static final Log log = LogFactory.getLog(TraceListProcessor.class);
    public TraceListProcessor(Properties properties) {
    }

    long page = 0L;
    long offset = 0L;

    public void processListBegin(RecordsList recordsList) {
        log.info("Page: " + page + " Offset: " + offset);
        offset += recordsList.size();
        ++page;
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        log.info("TOTAL:\nPage: " + page + " Offset: " + offset);
    }

    public void processListError(Exception e) {
        log.error(e);
        processListFinish();
    }
}
