package com.ontotext.oai.europeana.db.mongodb;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.ontotext.oai.europeana.db.RecordsProvider;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

/**
 * Created by Simo on 14-2-13.
 */
public class RecordsDb implements RecordsProvider {
    private EdmMongoServer edmServer;
    private static final Log log = LogFactory.getLog(RecordsDb.class);

    public RecordsDb(Properties properties) {
        String hostURLs = properties.getProperty("RecordsDb.URLs", "localhost");
        String databaseName = properties.getProperty("RecordsDb.db", "europeana");
        String username = properties.getProperty("RecordsDb.username", "");
        String password = properties.getProperty("RecordsDb.password", "");

        try {
           
            
            List<ServerAddress> addressesProduction = new ArrayList<ServerAddress>();
            for (String mongoStr : hostURLs.split(",")) {
                ServerAddress address;
                try {
                    address = new ServerAddress(mongoStr, 27017);
                    addressesProduction.add(address);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            Mongo mongo = new Mongo(addressesProduction);
            
            
            edmServer = new EdmMongoServerImpl(mongo, databaseName, username, password);
            log.info("RecordsDb: [" + hostURLs + "] db: " + databaseName);
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
