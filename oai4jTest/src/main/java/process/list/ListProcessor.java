package process.list;

import process.Processor;
import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-2-11.
 */
public class ListProcessor implements Processor<RecordsList> {
    public void process(RecordsList recordsList) {
        // do nothing
    }

    public Object total() {
        return null;
    }
}
