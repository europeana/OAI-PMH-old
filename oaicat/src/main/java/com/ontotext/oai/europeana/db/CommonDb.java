package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.http.EuropeanaDb;
import com.ontotext.oai.europeana.db.mongodb.RecordsDb;
import com.ontotext.oai.europeana.db.solr.SolrRegistry;

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class CommonDb {
    private final SetsProvider sets;
    private final RecordsRegistry registry;
    private final RecordsProvider records;

    // common methods
    public CommonDb(Properties properties) {
        EuropeanaDb europeanaDb = new EuropeanaDb(properties);
        sets = europeanaDb;
        registry = new SolrRegistry(properties);
        boolean disabledRecordsDb = Boolean.parseBoolean(properties.getProperty("RecordsDb.disabled", "false"));
        if (disabledRecordsDb) {
            records = europeanaDb; // for development purposes only
        } else {
            records = new RecordsDb(properties);
        }
    }

    public synchronized void close() {
        records.close();
        registry.close();
        sets.close();
    }

    // EuropeanaDb methods
    public synchronized String getRecord(String id) {
        return records.getRecord(id);
    }

    public synchronized Iterator<DataSet> listSets() {
        return sets.listSets();
    }

    // EuropeanaRegistry methods
    public synchronized RegistryInfo getRegistryInfo(String recordId) {
        return registry.getRegistryInfo(recordId);
    }

    public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String setId) {
        return registry.listRecords(from, until, setId);
    }
}
