package com.ontotext.oai.util;

import java.util.Iterator;

/**
 * Created by Simo on 14-1-23.
 */
public class CounterAdapterIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;
    private int count;
    public CounterAdapterIterator(Iterator<E> iterator,  int count) {
        this.iterator = iterator;
        this.count = count;
    }
    @Override
    public boolean hasNext() {
        if (count > 0) {
            --count;
            return iterator.hasNext();
        }
        return false;
    }

    @Override
    public E next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
