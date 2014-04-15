package walk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import process.ListProcessor;
import process.RecordProcessor;
import query.QueryListRecords;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

/**
 * Created by Simo on 14-1-30.
 */
public class ListRecordsWalker implements Runnable {
    private final OaiPmhServer server;
    public final RecordProcessor recordProcessor;
    public final ListProcessor listProcessor;
    private final QueryListRecords query;
    private final Navigator<RecordsList> navigator;
    Log log = LogFactory.getLog(ListRecordsWalker.class);

    public ListRecordsWalker(OaiPmhServer server,
                             RecordProcessor recordProcessor,
                             ListProcessor listProcessor,
                             QueryListRecords query,
                             Navigator<RecordsList> navigator) {
        this.server = server;
        this.recordProcessor = recordProcessor;
        this.listProcessor = listProcessor;
        this.query = query;
        this.navigator = navigator;
    }

    public void runThrow() throws OAIException {
        RecordsList recordsList = listRecords(query);

        do {
            navigator.check(recordsList);
            if (navigator.shouldStop()) {
                break;
            }
            listProcessor.processListBegin(recordsList);
            for (Record record : recordsList.asList()) {
                recordProcessor.processRecord(record);
            }
            listProcessor.processListEnd(recordsList);
            ResumptionToken resumptionToken = recordsList.getResumptionToken();
            if (resumptionToken == null) {
                break;
            }
            recordsList = server.listRecords(resumptionToken);
        } while (true);
    }

    public void run() {
        try {
            runThrow();
        } catch (OAIException e) {
            listProcessor.processListError(e);
        }
    }

    private RecordsList listRecords(QueryListRecords query) throws OAIException {
        return server.listRecords(query.prefix, query.from, query.until, query.set);
    }
}
