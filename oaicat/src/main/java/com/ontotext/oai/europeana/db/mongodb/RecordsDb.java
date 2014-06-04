package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.ontotext.oai.europeana.db.RecordsProvider;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.utils.EdmUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * Created by Simo on 14-2-13.
 */
public class RecordsDb implements RecordsProvider {
    private EdmMongoServer edmServer;
    private Log log = LogFactory.getLog(RecordsDb.class);
    private boolean debug;

    public RecordsDb(Properties properties) {
        debug = Boolean.parseBoolean(properties.getProperty("RecordsDb.debug", "false"));
        String host = properties.getProperty("RecordsDb.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("RecordsDb.port", "27017"));
        String databaseName = properties.getProperty("RecordsDb.db", "europeana");
        String username = properties.getProperty("RecordsDb.username", "");
        String password = properties.getProperty("RecordsDb.password", "");

        try {
            Mongo mongo = new MongoClient(host, port);
            edmServer = new EdmMongoServerImpl(mongo, databaseName, username, password);
            log.info("Using RecordsDb at " + host + ":" + port);
        } catch (Exception e) {
            log.error(e);
        }
    }
    // id: /11601/database_detail_php_ID_187548 ->
    // http://europeana.eu/api/v2/record/11601/database_detail_php_ID_187548.rdf?wskey=api2demo
    public String getRecord(String id) {
        if (debug) {
            log.info("getRecord(" + id + ")");
        }
        String rdf = null;
        try {
            FullBeanImpl fullBean = (FullBeanImpl) edmServer.getFullBean(id);
            if (fullBean != null) {
                rdf = EdmUtils.toEDM(fullBean, false);
            } else {
                log.error("No record: " + id);
            }

        } catch (Exception e) {
            log.error(e);
        }

        return rdf;
    }

    public void close() {
        edmServer.close();
    }

    public static void main(String[] args) {
//        String recordId = "http://data.europeana.eu/item/11601/HERBARW_NHMV_AUSTRIA_187548";
        String recordId = "/item/91647/2A35E4A2EA4495D0A5F3DEC6B9D92E13E17C7D81";
//        String recordId = "/11601/database_detail_php_ID_187548";
        Properties properties = new Properties();
        RecordsDb db = new RecordsDb(properties);
        try {
            String rdf = db.getRecord(recordId);
            System.out.println(rdf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.close();
        }
    }
}
