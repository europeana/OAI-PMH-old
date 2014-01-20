package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.*;

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

    public void close() {
        europeanaRegistry.close();
        europeanaDb.close();
    }

    // EuropeanaDb methods
    public String getRecord(String id) {
        return europeanaDb.getRecord(id);
    }

    public List<DataSet> listSets() {
        return europeanaDb.listSets();
    }

    // EuropeanaRegistry methods
    public RegistryInfo getRegistryInfo(String recordId) {
        return europeanaRegistry.getRegistryInfo(recordId);
    }

}
