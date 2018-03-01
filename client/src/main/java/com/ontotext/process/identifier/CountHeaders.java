package com.ontotext.process.identifier;

import com.ontotext.process.HeaderProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import se.kb.oai.pmh.Header;

public class CountHeaders implements HeaderProcessor {
    private static final Log log = LogFactory.getLog(CountHeaders.class);
    private int headers = 0;

    @Override
    public void processHeader(Header header) {
        if (header != null && header.getIdentifier() != null) {
            headers++;
        }
    }

    @Override
    public void processHeaderEnd() {
        log.info("Total headers: " + headers);
    }
}
