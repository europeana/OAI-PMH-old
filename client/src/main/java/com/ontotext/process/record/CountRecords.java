package com.ontotext.process.record;

import com.ontotext.process.RecordProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.Record;

import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public class CountRecords implements RecordProcessor {
    private static final Log log = LogFactory.getLog(CountRecords.class);
    private int nullRecords = 0;
    private int goodRecords = 0;
    public CountRecords(Properties properties) {
    }

    public void processRecord(Record record) {
        if (record == null || record.getMetadata() == null) {
            ++nullRecords;
        } else {
            ++goodRecords;
        }
    }

    public void processRecordEnd() {
        log.info("TOTAL Records:\n Good: " + goodRecords + " Bad: " + nullRecords);
    }


}
