package com.ontotext.oai.europeana.db;

/**
 * Created by Simo on 14-2-13.
 */
public interface RecordsProvider {
    public String getRecord(String id);
    public void close();
}
