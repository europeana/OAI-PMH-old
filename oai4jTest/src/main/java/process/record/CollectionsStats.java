package process.record;

import org.apache.commons.lang3.tuple.MutablePair;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simo on 14-2-11.
 */
public class CollectionsStats extends RecordProcessor {
    Map<String, MutablePair<Integer, Integer>> statsMap = new HashMap<String, MutablePair<Integer, Integer>>(20000);

    public void process(Record record) {
        boolean good = record.getMetadata() != null;
        Header header = record.getHeader();
        if (header != null) {
            add(header.getSetSpecs(), good);
        }
    }

    public Object total() {
        for (Map.Entry<String, MutablePair<Integer, Integer>> entry : statsMap.entrySet()) {
            MutablePair<Integer, Integer> good_bad = entry.getValue();
            int good = good_bad.left;
            int bad = good_bad.right;
            System.out.println(entry.getKey() + "\t" + good + "\t" + bad);
        }
        return null;
    }

    private void add(List<String> setId, boolean good) {
        if (setId.size() == 1) {
            add(setId.get(0), good);
        } else {
            System.out.println("Not one Id! Size(): " + setId.size());
        }
    }

    private void add(String setId, boolean good) {
        MutablePair<Integer, Integer> statsEntry = statsMap.get(setId);
        if (statsEntry == null) {
            statsEntry = MutablePair.of(0, 0);
            statsMap.put(setId,  statsEntry);
        }
        if (good) {
            statsEntry.setLeft(statsEntry.getLeft() + 1);
        } else {
            statsEntry.setRight(statsEntry.getRight() + 1);
        }
    }
}
