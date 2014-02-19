package process.record;

import se.kb.oai.pmh.Record;

import java.io.PrintStream;

/**
 * Created by Simo on 14-1-30.
 */
public class CountRecords extends RecordProcessor {
    private int nullRecords = 0;
    private int goodRecords = 0;
    private final PrintStream out;

    public CountRecords(PrintStream out) {
        this.out = out;
    }

    public void process(Record record) {
        if (record.getMetadata() == null) {
            ++nullRecords;
        } else {
            ++goodRecords;
        }
    }

    public Object total() {
        out.println("TOTAL Records:\n Good: " + goodRecords + " Bad: " + nullRecords);
        return out;
    }


}
