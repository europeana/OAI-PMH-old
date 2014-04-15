package com.ontotext.iterator;

import se.kb.oai.pmh.Set;

import java.util.Iterator;

/**
 * Created by Simo on 14-2-17.
 */
public class SetsIteratorAdaptor implements Iterator<String> {
    private final Iterator<Set> itSet;

    public SetsIteratorAdaptor(Iterator<Set> itSet) {
        this.itSet = itSet;
    }
    public boolean hasNext() {
        return itSet.hasNext();
    }

    public String next() {
        return itSet.next().getSpec();
    }

    public void remove() {
        // do nothing
    }
}
