package com.ontotext.oai.europeana.db;

import java.io.Closeable;
import java.util.Iterator;

/**
 * Created by Simo on 4.6.2014 г..
 */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
    public void close(); // no throw version.
}
