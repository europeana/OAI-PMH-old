package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * If enabled in the client.properties this logs 1 line after every page load
 * Created by Simo on 14-1-30.
 */
public class TraceListProcessor implements ListProcessor<RecordsList> {
    private static final Log log = LogFactory.getLog(TraceListProcessor.class);

    long page;
    long offset;

    public TraceListProcessor(Properties properties) {
        reset();
    }

    public void reset() {
        page = 0L;
        offset = 0L;
    }


    public void processListBegin(RecordsList recordsList) {
        log.info("Page: " + page + " Offset: " + offset);
        offset += recordsList.size();
        ++page;
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        log.info("TOTAL:\nPage: " + page + " Offset: " + offset);
        reset();
    }

    public void processListError(Exception e) {
        log.error(e);
        processListFinish();
    }
}
