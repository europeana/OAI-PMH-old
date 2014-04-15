package process.list;

import process.ListProcessor;
import process.RecordProcessor;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-27.
 */
class IterateRecords implements ListProcessor {
    private final RecordProcessor recordProcessor;

    public IterateRecords(RecordProcessor recordProcessor) {
        this.recordProcessor = recordProcessor;
    }

    public void processListBegin(RecordsList recordsList) {
        for (Record record : recordsList.asList()) {
            recordProcessor.processRecord(record);
        }
    }

    public void processListEnd(RecordsList recordsList) {

    }

    public void processListFinish() {
        recordProcessor.processRecordEnd();
    }

    public void processListError(Exception e) {

    }
}
