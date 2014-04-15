package process;

import se.kb.oai.pmh.Record;

/**
 * Created by Simo on 14-2-27.
 */
public interface RecordProcessor {
    void processRecord(Record record);
    void processRecordEnd();
}
