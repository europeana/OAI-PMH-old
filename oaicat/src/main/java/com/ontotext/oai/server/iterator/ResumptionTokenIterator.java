package com.ontotext.oai.server.iterator;

import ORG.oclc.oai.server.catalog.RecordFactory;
import com.ontotext.oai.ResumptionToken;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.util.Callback;

import java.util.Iterator;

/**
 * Created by Simo on 16.4.2014 г..
 */
abstract public class ResumptionTokenIterator implements Iterator<String> {
    private final ResumptionToken token;
    protected final RecordFactory recordFactory;
    private final int limit;
    private int num = 0;
    private Callback removeResumptionToken;
    RegistryInfo cachedValue = null; // TODO: temp until 'deleted' flag is fixed.

    public ResumptionTokenIterator(ResumptionToken token, RecordFactory recordFactory, int limit) {

        this.token = token;
        this.recordFactory = recordFactory;
        this.limit = limit;
    }

    @Override
    public boolean hasNext() {
        if (cachedValue != null) {
            return true;
        }

        if (num < limit) {
            // TODO: temp. Skip 'deleted' and cache first not deleted.
            while (token.hasNext()) {
                cachedValue = token.next();
                if (!cachedValue.deleted) {
                    return true;
                }
            }
        } else {
            return false;
        }

        if (removeResumptionToken != null) {
            removeResumptionToken.callback();
            removeResumptionToken = null;
        }
        return false;
    }

    @Override
    public String next() {
        String header = null;

        if (cachedValue != null) {
            header = convert(cachedValue);
            cachedValue = null;
            ++num;
        }

        return header;
    }

    @Override
    public void remove() {
        // not supported
    }



    abstract String convert(RegistryInfo registryInfo);
    public void setCallback(Callback callback) {
        this.removeResumptionToken = callback;
    }
}
