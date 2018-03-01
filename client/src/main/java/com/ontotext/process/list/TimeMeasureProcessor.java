package com.ontotext.process.list;

import com.ontotext.process.ListProcessor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * Created by Simo on 14-2-11.
 */
public class TimeMeasureProcessor implements ListProcessor<RecordsList> {

    private static final Logger LOG = LogManager.getLogger(TimeMeasureProcessor.class);

    long lastTime = System.currentTimeMillis();
    long totalTime = 0L;
    private long listCount = 0L;

    public TimeMeasureProcessor(Properties properties) {
    }

    public void processListBegin(RecordsList recordsList) {
        long time = System.currentTimeMillis();
        long diff = time - lastTime;
        totalTime += diff;
        ++listCount;
        lastTime = time;
        LOG.info("{} ms", diff);
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        LOG.info("Total pages: " + listCount);
        LOG.info("Total time: " + DurationFormatUtils.formatDuration(totalTime, "HH:mm:ss.SSS"));
        if (listCount > 0) {
            LOG.info("Average time: " + DurationFormatUtils.formatDuration(totalTime / listCount, "mm:ss.SSS"));
        }
    }

    public void processListError(Exception e) {
        processListFinish();
    }
}
