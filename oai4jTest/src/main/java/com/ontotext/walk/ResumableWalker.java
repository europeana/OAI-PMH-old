package walk;

import query.ListRecordsParamsImpl;
import query.QueryListRecords;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Simo on 14-2-20.
 */
public class ResumableWalker implements Resumable {
    private final OaiPmhServer server;
    private ListRecordsParamsImpl query;
    private Queue<RecordsList> queue;
    private volatile int status = STATUS_NEW;

    private static final int STATUS_NEW = 0;
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_DONE = 3;


    public ResumableWalker(OaiPmhServer server, QueryListRecords query, Queue<RecordsList> queue) {
        this.server = server;
        this.query = new ListRecordsParamsImpl(query);
        this.queue = queue;
    }
    public boolean isDone() {
        return (status == STATUS_DONE);
    }

    public boolean isPaused() {
        return (status == STATUS_PAUSED);
    }

    public void pause() {
        status = STATUS_PAUSED;
    }

    public void resume() {
        run();
    }

    public void stop() {
        status = STATUS_DONE;
    }

    public void run() {
        RecordsList rlLast = null;
        try {
            status = STATUS_RUNNING;
            RecordsList rl = listRecords(query.asQuery());

            do {
                rlLast = rl;
                queue.add(rl);
                ResumptionToken resumptionToken = rl.getResumptionToken();
                if (resumptionToken == null) {
                    break;
                }
                rl = server.listRecords(resumptionToken);
            } while (status == STATUS_RUNNING);

        } catch (Exception e) {
            status = STATUS_PAUSED;
            saveState(rlLast);
        }
    }

    private RecordsList listRecords(QueryListRecords query) throws OAIException {
        return server.listRecords(query.prefix, query.from, query.until, query.set);
    }

    private void saveState(RecordsList rlLast) {
        if (rlLast != null) {
            List<Record> records = rlLast.asList();
            if (!records.isEmpty()) {
                Record lastRecord = records.get(records.size()-1);
                if (lastRecord != null) {
                    query.setFrom(lastRecord.getHeader().getDatestamp());
                }
            }
        }
    }


    private static final String host = "http://localhost:8080/oaicat/OAIHandler";
    private static final int QUEUE_SIZE = 100;

    public static void main(String[] args) {
        OaiPmhServer server = new OaiPmhServer(host);
        QueryListRecords query = new QueryListRecords(null, null, null);
        BlockingQueue<RecordsList> queue = new ArrayBlockingQueue<RecordsList>(QUEUE_SIZE);

        ResumableWalker walker = new ResumableWalker(server, query, queue);
    }
}
