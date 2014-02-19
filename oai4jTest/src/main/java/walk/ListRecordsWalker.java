package walk;

import process.list.ListProcessor;
import process.record.RecordProcessor;
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
        RecordsList rl = listRecords(query);
        listProcessor.process(rl);

        do {

            navigator.check(rl);
            if (navigator.shouldStop()) {
                break;
            }
            listProcessor.process(rl);
            for (Record record : rl.asList()) {
                recordProcessor.process(record);
            }
            ResumptionToken resumptionToken = rl.getResumptionToken();
            if (resumptionToken == null) {
                break;
            }
            rl = server.listRecords(resumptionToken);
        } while (true);
    }

    public void run() {
        try {
            runThrow();
        } catch (OAIException e) {
            e.printStackTrace();
        }
    }

    private RecordsList listRecords(QueryListRecords query) throws OAIException {
        return server.listRecords(query.metadataPrefix, query.from, query.until, query.set);
    }
}
