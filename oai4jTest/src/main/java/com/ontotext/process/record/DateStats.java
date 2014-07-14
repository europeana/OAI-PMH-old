package com.ontotext.process.record;

import com.ontotext.process.RecordProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;

import java.util.Properties;

/**
 * Created by Simo on 14-3-4.
 */
public class DateStats implements RecordProcessor {
    private static Log log = LogFactory.getLog(DateStats.class);
    private int flushCount;
    long count = 0;

    public DateStats(Properties properties) {
        flushCount = Integer.parseInt(properties.getProperty("DateStats.flushCount", "0"));
    }

    public void processRecord(Record record) {
        Header header = record.getHeader();
        if (header != null) {
            String date = header.getDatestamp();
            ++count;
            if (flushCount == 0 || count % flushCount == 0) {
                log.info(date);
            }
        }
    }

    public void processRecordEnd() {
        log.info("Num records: " + count);
    }

}
