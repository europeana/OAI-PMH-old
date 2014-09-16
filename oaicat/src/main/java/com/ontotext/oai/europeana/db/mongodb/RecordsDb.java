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
    private static final Log log = LogFactory.getLog(RecordsDb.class);

    public RecordsDb(Properties properties) {
        String host = properties.getProperty("RecordsDb.host", "localhost");
        int port = Integer.parseInt(properties.getProperty("RecordsDb.port", "27017"));
        String databaseName = properties.getProperty("RecordsDb.db", "europeana");
        String username = properties.getProperty("RecordsDb.username", "");
        String password = properties.getProperty("RecordsDb.password", "");

        try {
            Mongo mongo = new MongoClient(host, port);
            edmServer = new EdmMongoServerImpl(mongo, databaseName, username, password);
            log.info("RecordsDb: [" + host + ":" + port + "] db: " + databaseName);
        } catch (Exception e) {
            log.error("Records DB is not constructed!", e);
            throw new RuntimeException(e);
        }
    }
    // id: /11601/database_detail_php_ID_187548 ->
    // http://europeana.eu/api/v2/record/11601/database_detail_php_ID_187548.rdf?wskey=api2demo
    public String getRecord(String id) {
        log.debug("getRecord(" + id + ")");
        String rdf = null;
        try {
            FullBeanImpl fullBean = (FullBeanImpl) edmServer.getFullBean(id);
            if (fullBean != null) {
                rdf = EdmUtils.toEDM(fullBean, false);
            } else {
                log.error("No record: " + id);
            }

        } catch (Exception e) {
            log.error("Record: " + id, e);
        }

        return rdf;
    }

    public void close() {
        edmServer.close();
    }

}
