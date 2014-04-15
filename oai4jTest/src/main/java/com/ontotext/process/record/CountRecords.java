package com.ontotext.process.record;

import org.apache.commons.logging.LogFactory;
import com.ontotext.process.OutHolder;
import com.ontotext.process.RecordProcessor;
import se.kb.oai.pmh.Record;

import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public class CountRecords extends OutHolder implements RecordProcessor {
    private int nullRecords = 0;
    private int goodRecords = 0;
    public CountRecords(Properties properties) {
        super(properties.getProperty("CountRecords.logFile"),  LogFactory.getLog(CountRecords.class));
    }

    public void processRecord(Record record) {
        if (record.getMetadata() == null) {
            ++nullRecords;
        } else {
            ++goodRecords;
        }
    }

    public void processRecordEnd() {
        out.println("TOTAL Records:\n Good: " + goodRecords + " Bad: " + nullRecords);
    }


}
