package com.ontotext.process;

import se.kb.oai.pmh.Header;

public interface HeaderProcessor {
    void processHeader(Header header);
    void processHeaderEnd();
}
