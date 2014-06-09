package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.DataSet;

import java.util.Iterator;

/**
 * Created by Simo on 9.6.2014 г..
 */
public interface SetsProvider {
    Iterator<DataSet> listSets();
    void close();
}
