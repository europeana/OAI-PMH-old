package com.ontotext.process.record;

import com.ontotext.process.RecordProcessor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Simo on 14-2-11.
 */
public class CollectionsStats implements RecordProcessor {
    private static Log log = LogFactory.getLog(CollectionsStats.class);
    Map<String, MutablePair<Integer, Integer>> statsMap = new HashMap<String, MutablePair<Integer, Integer>>(20000);

    public CollectionsStats(Properties properties) {
    }

    public void processRecord(Record record) {
        boolean good = record.getMetadata() != null;
        Header header = record.getHeader();
        if (header != null) {
            add(header.getSetSpecs(), good);
        }
    }

    public void processRecordEnd() {
        for (Map.Entry<String, MutablePair<Integer, Integer>> entry : statsMap.entrySet()) {
            MutablePair<Integer, Integer> good_bad = entry.getValue();
            int good = good_bad.left;
            int bad = good_bad.right;
            log.info(entry.getKey() + "\t" + good + "\t" + bad);
        }
    }

    private void add(List<String> setId, boolean good) {
        if (setId.size() == 1) {
            add(setId.get(0), good);
        } else {
            log.error("Not one Id! Size(): " + setId.size());
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
