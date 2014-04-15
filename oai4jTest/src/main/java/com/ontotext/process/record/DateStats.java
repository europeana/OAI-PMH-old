package com.ontotext.process.record;

import org.apache.commons.logging.LogFactory;
import com.ontotext.process.OutHolder;
import com.ontotext.process.RecordProcessor;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;

import java.util.Properties;

/**
 * Created by Simo on 14-3-4.
 */
public class DateStats extends OutHolder implements RecordProcessor {
    private String firstDate = null;
    private String lastDate = null;
    private int flushCount;
    long count = 0;

    public DateStats(Properties properties) {
        super(properties.getProperty("DateStats.logFile"), LogFactory.getLog(DateStats.class));
        flushCount = Integer.parseInt(properties.getProperty("DateStats.flushCount", "0"));
    }

    public void processRecord(Record record) {
        Header header = record.getHeader();
        if (header != null) {
            String date = header.getDatestamp();
            if (firstDate == null) {
                firstDate = date;
            }

            lastDate = date;
            if (flushCount != 0) {
                if (++count % flushCount == 0) {
                    print();
                    out.flush();
                }
            }
        }
    }

    public void processRecordEnd() {
        print();
        close();
    }

    public void print() {
        out.println(getMinDate());
        out.println(getMaxDate());
        out.println();
    }

    public String getMinDate() {
        return firstDate;
    }

    public String getMaxDate() {
        return lastDate;
    }
}
