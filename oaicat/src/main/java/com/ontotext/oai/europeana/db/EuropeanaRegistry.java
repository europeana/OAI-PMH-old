package com.ontotext.oai.europeana.db;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.ontotext.oai.europeana.RegistryInfo;
import eu.europeana.corelib.solr.MongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdRegistry;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdRegistryMongoServerImpl;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by Simo on 14-1-10.
 */
public class EuropeanaRegistry {
    private DB mongoDb;
    private MongoServer mongoServer;

    private DBCollection registry;
    private MongoUtil mongoUtil = new MongoUtil();

    public EuropeanaRegistry(Properties properties) {
        String host = properties.getProperty("MongoDbCatalog.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("MongoDbCatalog.port", "27017"));
        String dbName = properties.getProperty("EdmOaiHandler.db", "EuropeanaIdRegistry");
        String registryName = properties.getProperty("MongoDbCatalog.collection", "EuropeanaIdRegistry");
        try {
            MongoClient mongoClient = new MongoClient(host, port);
            mongoServer = new EuropeanaIdRegistryMongoServerImpl(mongoClient, dbName);
            mongoDb = mongoClient.getDB(dbName);
            registry = mongoDb.getCollection(registryName);
            registry.setObjectClass(RegistryRecord.class);
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

    public DBCursor listRecords(Date from, Date until, String setId) {
        return registry.find(mongoUtil.queryDateRange(setId,  from, until));
    }

    public Iterator<EuropeanaIdRegistry> listRecords1(Date from, Date until, String setId) {
        Datastore dataStore = mongoServer.getDatastore();
        Query<EuropeanaIdRegistry> query = dataStore.createQuery(EuropeanaIdRegistry.class);
        if (setId != null) {
            query.filter("=", setId);
        }
        query.filter("last_checked >= ", from)
             .filter("last_checked < ", until);

        return  query.iterator();
    }

    public void close() {
        mongoServer.close();
    }


    // test
    static final int rangeMonths = 0;
    static final int rangeDays = 10;

    static final DateTime startDate = new DateTime("2013-01-01T00:00:00Z");
    static final DateTime endDate = new DateTime();
    static int LOG_RATE = 0;
//    static final int LOG_RATE = 100000;

    private static void testList(EuropeanaRegistry er) {
        for (DateTime date = startDate; date.isBefore(endDate); ) {
            DateTime from = date;
            DateTime until = date.plusMonths(rangeMonths).plusDays(rangeDays);
            date = until;

            System.out.println("From : " + from.toDate());
            System.out.println("Until: " + until.toDate());

            DBCursor it = er.listRecords(from.toDate(), until.toDate(), null);
            try {
                int count = 0;
                while (it.hasNext()) {
                    ++count;
                    RegistryRecord record = (RegistryRecord)it.next();
                    if (LOG_RATE != 0 && count % LOG_RATE == 0) {
                        System.out.println(record.last_checked());
                    }
                }
                System.out.println(count);
            } finally {
                it.close();
            }
        }
    }

    private static void testList1(EuropeanaRegistry er) {
        for (DateTime date = startDate; date.isBefore(endDate); ) {
            DateTime from = date;
            DateTime until = date.plusMonths(rangeMonths).plusDays(rangeDays);
            date = until;

            System.out.println("From : " + from.toDate());
            System.out.println("Until: " + until.toDate());

            Iterator<EuropeanaIdRegistry> it = er.listRecords1(from.toDate(), until.toDate(), null);
            int count = 0;
            while (it.hasNext()) {
                ++count;
                EuropeanaIdRegistry entry = it.next();
                if (LOG_RATE != 0 && count % LOG_RATE == 0) {
                    System.out.println(entry.getLast_checked());
                }
            }

            System.out.println(count);
        }
    }

//    static int offset = 6;

    private static final String crawler4 = "192.168.130.198";
    private static final String host = crawler4;
//    private static final String host = "localhost";
    private static boolean runNew = false;
    private static boolean runOld = true;


    public static void main(String[] args) {
        StopWatch sw = new StopWatch();
        Properties properties = new Properties();
        properties.put("MongoDbCatalog.host", host);
        EuropeanaRegistry er = new EuropeanaRegistry(properties);

        try {
            String timeOld = null;
            String timeNew = null;
            if (runNew) {
                sw.start();
                testList1(er);
                sw.stop();
                timeNew = sw.toString();
            }

            if (runOld) {
                sw.reset();
                sw.start();
                testList(er);
                sw.stop();
                timeOld = sw.toString();
            }

            if (timeNew != null) {
                System.out.println("Time new: " + timeNew);
            }

            if (timeOld != null) {
                System.out.println("Time old: " + timeOld);
            }
        } finally {
            er.close();
        }
    }
}
