package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.*;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import com.ontotext.oai.europeana.db.RecordsRegistry;
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Properties;


/**
 * Connect to Europeana Registry (Mongo) database
 * Created by Simo on 14-1-10.
 */
public class EuropeanaRegistry implements RecordsRegistry {

    private static final Logger LOG = LogManager.getLogger(EuropeanaRegistry.class);

    private DB mongoDb;
    private MongoClient mongoClient;
    private DBCollection registry;
    private MongoUtil mongoUtil = new MongoUtil();
    private int batchSize;

    public EuropeanaRegistry(Properties properties) {
        String hostURLs = properties.getProperty("mongo.host");
        String hostPorts = properties.getProperty("mongo.port");
        String username = properties.getProperty("mongo.username");
        String password = properties.getProperty("mongo.password");
        String databaseName = properties.getProperty("mongo.registry.dbname");
        String registryName = properties.getProperty("mongo.registry.collection", "EuropeanaIdRegistry");
        batchSize = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "300"));

        mongoClient = new MongoProviderImpl(hostURLs, hostPorts, databaseName, username, password).getMongo();
        LOG.info("Connected to Mongo record database {} on [{}]", databaseName, hostURLs);
        mongoDb = mongoClient.getDB(databaseName);
        registry = mongoDb.getCollection(registryName);
        registry.setObjectClass(MongoRecordsRegistry.class);
        //registry.ensureIndex(mongoUtil.queryIndexDate());
        //registry.ensureIndex(mongoUtil.queryIndexSetDate());
    }
    public RegistryInfo getRegistryInfo(String recordId) {
        DBCursor dbCursor = registry.find(mongoUtil.queryRecord(recordId));
        RegistryInfo registryInfo = null;
        try {
            if (dbCursor.hasNext()) {
                MongoRecordsRegistry record = (MongoRecordsRegistry) dbCursor.next();
                String cid = record.cid();
                Date date = record.last_checked();
                boolean deleted = record.deleted();
                registryInfo =  new RegistryInfo(cid, recordId, date, deleted);
            }
        } finally {
            dbCursor.close();
        }

        return registryInfo;
    }

    public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String setId) {
        return new MongoRegistryIterator(registry.find(mongoUtil.queryDateRange(setId,  from, until)).batchSize(batchSize));
    }

    public void close() {
        mongoClient.close();
    }


    // /000002/10978_0F3716A7_DB96_403F_ADDE_399D29E34937 -> 000002
    private static String parseCid(String localId) {
        String cid = null;
        if (localId != null) {
            int pos = localId.indexOf('/', 1);
            if (pos >= 0) {
                int pos1 = localId.indexOf('/', pos + 1);
                if (pos1 >= 0) {
                    cid = localId.substring(pos,  pos1);
                }
            }
        }

        return cid;
    }
}
