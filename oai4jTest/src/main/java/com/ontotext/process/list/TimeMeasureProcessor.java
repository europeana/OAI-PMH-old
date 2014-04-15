package com.ontotext.process.list;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.logging.LogFactory;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.OutHolder;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * Created by Simo on 14-2-11.
 */
public class TimeMeasureProcessor extends OutHolder implements ListProcessor {
    long lastTime = System.currentTimeMillis();
    long totalTime = 0L;
    private long listCount = 0L;

    public TimeMeasureProcessor(Properties properties) {
        super(properties.getProperty("TimeMeasureProcessor.logFile"), LogFactory.getLog(TimeMeasureProcessor.class));
    }

    public void processListBegin(RecordsList recordsList) {
        long time = System.currentTimeMillis();
        long diff = time - lastTime;
        totalTime += diff;
        ++listCount;
        lastTime = time;
        out.println(diff);
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        out.println("Total pages: " + listCount);
        System.out.println("Total time: " + DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss.SSS"));
        out.println("Average time: " + DurationFormatUtils.formatDuration(totalTime/listCount, "mm:ss.SSS"));
        super.close();
    }

    public void processListError(Exception e) {
        processListFinish();
    }
}
