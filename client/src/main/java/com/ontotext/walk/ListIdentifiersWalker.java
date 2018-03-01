package com.ontotext.walk;

import com.ontotext.helper.WatchDog;
import com.ontotext.process.HeaderProcessor;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import com.ontotext.query.QueryListIdentifiers;
import com.ontotext.query.QueryListRecords;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.*;

public class ListIdentifiersWalker implements Runnable {
    private final OaiPmhServer server;
    public final HeaderProcessor headerProcessor;
    public final ListProcessor listProcessor;
    private final QueryListIdentifiers query;
    private final Navigator<IdentifiersList> navigator;
    Log log = LogFactory.getLog(ListIdentifiersWalker.class);
    WatchDog watchDog = new WatchDog(10);

    public ListIdentifiersWalker(OaiPmhServer server,
                                 HeaderProcessor headerProcessor,
                                 ListProcessor<IdentifiersList> listProcessor,
                                 QueryListIdentifiers query,
                                 Navigator<IdentifiersList> navigator) {
        this.server = server;
        this.headerProcessor = headerProcessor;
        this.listProcessor = listProcessor;
        this.query = query;
        this.navigator = navigator;
    }

    public void runThrow() throws OAIException {
        IdentifiersList identifiersList = listIdentifiers(query);

        do {
            navigator.check(identifiersList);
            if (navigator.shouldStop()) {
                break;
            }
            listProcessor.processListBegin(identifiersList);
            for (Header header : identifiersList.asList()) {
                headerProcessor.processHeader(header);
            }
            listProcessor.processListEnd(identifiersList);
            ResumptionToken resumptionToken = identifiersList.getResumptionToken();
            if (resumptionToken == null) {
                break;
            }
            identifiersList = listIdentifiers(resumptionToken);
        } while (identifiersList.size() > 0);
    }

    private IdentifiersList listIdentifiers(ResumptionToken resumptionToken) throws OAIException {
        IdentifiersList identifiersList = server.listIdentifiers(resumptionToken);
        watchDog.reset();
        return identifiersList;
    }

    private IdentifiersList listIdentifiers(QueryListIdentifiers query) throws OAIException {
        IdentifiersList identifiersList = server.listIdentifiers(query.getPrefix(), query.getFrom(), query.getUntil(), query.getSet());
        watchDog.reset();
        return identifiersList;
    }

    public void run() {
        try {
            runThrow();
        } catch (OAIException e) {
            log.error("Exiting ...", e);
            listProcessor.processListError(e);
        }
    }
}
