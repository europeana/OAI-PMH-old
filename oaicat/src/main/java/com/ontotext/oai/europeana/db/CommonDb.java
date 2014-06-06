package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.http.EuropeanaDb;
import com.ontotext.oai.europeana.db.mongodb.EuropeanaRegistry;
import com.ontotext.oai.europeana.db.mongodb.RecordsDb;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class CommonDb {
    private final EuropeanaDb europeanaDb;
    private final RecordsRegistry europeanaRegistry;
    private final RecordsProvider recordsDb;

    // common methods
    public CommonDb(Properties properties) {
        europeanaDb = new EuropeanaDb(properties);
        europeanaRegistry = new EuropeanaRegistry(properties);
        boolean disabledRecordsDb = Boolean.parseBoolean(properties.getProperty("RecordsDb.disabled", "false"));
        if (disabledRecordsDb) {
            recordsDb = europeanaDb; // for development purposes only
        } else {
            recordsDb = new RecordsDb(properties);
        }
    }

    public synchronized void close() {
        recordsDb.close();
        europeanaRegistry.close();
        europeanaDb.close();
    }

    // EuropeanaDb methods
    public synchronized String getRecord(String id) {
        return recordsDb.getRecord(id);
    }

    public synchronized List<DataSet> listSets() {
        return europeanaDb.listSets();
    }

    // EuropeanaRegistry methods
    public synchronized RegistryInfo getRegistryInfo(String recordId) {
        return europeanaRegistry.getRegistryInfo(recordId);
    }

    public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String setId) {
        return europeanaRegistry.listRecords(from, until, setId);
    }
}
