package com.ontotext.oai.europeana.db;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.ontotext.oai.europeana.RegistryInfo;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class EuropeanaRegistry {
    private DB mongoDb;
    private MongoClient mongoClient;
    private DBCollection registry;
    private MongoUtil mongoUtil = new MongoUtil();

    public EuropeanaRegistry(Properties properties) {
        String host = properties.getProperty("MongoDbCatalog.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("MongoDbCatalog.port", "27017"));
        String dbName = properties.getProperty("EdmOaiHandler.db", "EuropeanaIdRegistry");
        String registryName = properties.getProperty("MongoDbCatalog.collection", "EuropeanaIdRegistry");
        try {
            mongoClient = new MongoClient(host, port);
            mongoDb = mongoClient.getDB(dbName);
            registry = mongoDb.getCollection(registryName);
            registry.setObjectClass(RegistryRecord.class);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public RegistryInfo getRegistryInfo(String recordId) {
        DBCursor dbCursor = registry.find(mongoUtil.queryRecord(recordId));
        RegistryInfo registryInfo = null;
        try {
            if (dbCursor.hasNext()) {
                RegistryRecord record = (RegistryRecord) dbCursor.next();
                String cid = record.cid();
                Date date = record.last_checked();
                String orig = record.orig();
                registryInfo =  new RegistryInfo(cid, recordId, date, orig);
            }
        } finally {
            dbCursor.close();
        }

        return registryInfo;
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
