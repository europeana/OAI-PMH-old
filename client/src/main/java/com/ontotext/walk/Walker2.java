package com.ontotext.walk;

import com.ontotext.process.ListProcessor;
import com.ontotext.query.QueryListRecords;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.RecordsList;

/**
 * Created by Simo on 14-3-4.
 */
public class Walker2 implements Runnable {
    private final OaiPmhServer server;
    private QueryListRecords query;
    private ListProcessor processor;
    private RecordsList recordsList = null;


    public Walker2(OaiPmhServer server, QueryListRecords query, ListProcessor processor) {
        this.server = server;
        this.query = query;
        this.processor = processor;
    }

    public void run() {

//        boolean finished = false;
//        do {
//            if (firstPage()) {
//                do {
//                    pro
//                }
//            }
//
//        } while (!finished);

    }

    private boolean firstPage() {
        try {
            if (recordsList == null) {
                recordsList = listRecords(query);
            } else {

            }
        } catch (OAIException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean nextPage() {
        return false;
//        if ()
    }


    private RecordsList listRecords(QueryListRecords query) throws OAIException {
        return server.listRecords(query.prefix, query.from, query.until, query.set);
    }
}
