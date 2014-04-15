package process.list;

import org.apache.commons.logging.LogFactory;
import process.ListProcessor;
import process.OutHolder;
import se.kb.oai.pmh.RecordsList;

import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public class TraceListProcessor extends OutHolder implements ListProcessor {
    public TraceListProcessor(Properties properties) {
        super(properties.getProperty("TraceListProcessor.logFile"), LogFactory.getLog(TraceListProcessor.class));
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
        out.println("TOTAL:\nPage: " + page + " Offset: " + offset);
        super.close();
    }

    public void processListError(Exception e) {
        log.error(e);
        processListFinish();
    }
}
