package process.record;

import process.RecordProcessor;
import se.kb.oai.pmh.Record;

import java.util.List;

/**
 * Created by Simo on 14-2-27.
 */
public class RecordProcessorHub implements RecordProcessor {
    private final List<RecordProcessor> processors;

    public RecordProcessorHub(List<RecordProcessor> processors) {
        this.processors = processors;
    }
    public void processRecord(Record record) {
        for (RecordProcessor processor : processors) {
            processor.processRecord(record);
        }
    }

    public void processRecordEnd() {
        for (int i = processors.size(); --i >= 0; ) {
            RecordProcessor processor = processors.get(i);
            processor.processRecordEnd();
        }
    }
}
