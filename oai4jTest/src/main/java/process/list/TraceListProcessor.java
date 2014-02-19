package process.list;

import se.kb.oai.pmh.RecordsList;

import java.io.PrintStream;

/**
 * Created by Simo on 14-1-30.
 */
public class TraceListProcessor extends ListProcessor {
    private final PrintStream out;
    long page = 0L;
    long offset = 0L;

    public TraceListProcessor(PrintStream out) {
        this.out = out;
    }

    public void process(RecordsList recordsList) {
        out.println("Page: " + page + " Offset: " + offset);
        offset += recordsList.size();
        ++page;
    }

    public Object total() {
        out.println("TOTAL:\nPage: " + page + " Offset: " + offset);
        return out;
    }
}
