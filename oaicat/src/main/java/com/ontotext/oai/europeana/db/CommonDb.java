package com.ontotext.oai.europeana.db;

import com.mongodb.DBCursor;
import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class CommonDb {
    private final EuropeanaDb europeanaDb;
    private final EuropeanaRegistry europeanaRegistry;

    // common methods
    public CommonDb(Properties properties) {
        europeanaDb = new EuropeanaDb(properties);
        europeanaRegistry = new EuropeanaRegistry(properties);
    }

    public synchronized void close() {
        europeanaRegistry.close();
        europeanaDb.close();
    }

    // EuropeanaDb methods
    public synchronized String getRecord(String id) {
        return europeanaDb.getRecord(id);
    }

    public synchronized List<DataSet> listSets() {
        return europeanaDb.listSets();
    }

    // EuropeanaRegistry methods
    public synchronized RegistryInfo getRegistryInfo(String recordId) {
        return europeanaRegistry.getRegistryInfo(recordId);
    }

    public DBCursor listRecords(Date from, Date until, String setId) {
        return europeanaRegistry.listRecords(from, until, setId);
    }
}
