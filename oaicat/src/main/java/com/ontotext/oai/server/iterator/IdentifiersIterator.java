package com.ontotext.oai.server.iterator;

import ORG.oclc.oai.server.catalog.RecordFactory;
import com.ontotext.oai.ResumptionToken;
import com.ontotext.oai.europeana.RegistryInfo;

/**
 * Created by Simo on 16.4.2014 г..
 */
public class IdentifiersIterator extends ResumptionTokenIterator {
    public IdentifiersIterator(ResumptionToken token, RecordFactory recordFactory, int limit) {
        super(token, recordFactory, limit);
    }

    @Override
    String convert(RegistryInfo registryInfo) {
        System.out.println("Identifiers");
        return recordFactory.getOAIIdentifier(registryInfo);
    }
}
