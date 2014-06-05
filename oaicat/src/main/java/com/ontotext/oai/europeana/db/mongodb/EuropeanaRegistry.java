package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.*;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import com.ontotext.oai.europeana.db.mongodb.MongoRecordsRegistry;
import com.ontotext.oai.europeana.db.RecordsRegistry;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class EuropeanaRegistry implements RecordsRegistry {
    private DB mongoDb;
    private MongoClient mongoClient;
    private DBCollection registry;
    private MongoUtil mongoUtil = new MongoUtil();
    private int batchSize;

    public EuropeanaRegistry(Properties properties) {
        String host = properties.getProperty("EuropeanaRegistry.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("EuropeanaRegistry.port", "27017"));
        String dbName = properties.getProperty("EuropeanaRegistry.db", "EuropeanaIdRegistry");
        String registryName = properties.getProperty("EuropeanaRegistry.collection", "EuropeanaIdRegistry");
        String username = properties.getProperty("EuropeanaRegistry.username");
        String password = properties.getProperty("EuropeanaRegistry.password");
        batchSize = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "100"));
        try {
            List<MongoCredential> credentials = null;
            if (password != null && username != null) {
                credentials = Arrays.asList(MongoCredential.createMongoCRCredential(username, dbName, password.toCharArray()));
            }

            mongoClient = new MongoClient(new ServerAddress(host, port), credentials);
            mongoDb = mongoClient.getDB(dbName);
            registry = mongoDb.getCollection(registryName);
            registry.setObjectClass(MongoRecordsRegistry.class);
            registry.ensureIndex(mongoUtil.queryIndexDate());
            registry.ensureIndex(mongoUtil.queryIndexSetDate());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
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
        return new MongoDbCurosr<RegistryInfo>(registry.find(mongoUtil.queryDateRange(setId,  from, until)).batchSize(batchSize));
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
