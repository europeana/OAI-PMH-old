package com.ontotext.oai.europeana.db.mongodb;


import java.util.Properties;

import com.ontotext.oai.europeana.db.RecordsProvider;

import com.ontotext.oai.util.StringUtil;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Simo on 14-2-13.
 */
public class RecordsDb implements RecordsProvider {

    private static final Logger LOG = LogManager.getLogger(RecordsDb.class);

    private static final int WARN_RECORD_RETRIEVAL_DURATION = 3000; // in ms

    private EdmMongoServer edmServer;

    public RecordsDb(Properties properties) {
        String hostURLs = properties.getProperty("mongo.host");
        String hostPorts = properties.getProperty("mongo.port");
        String username = properties.getProperty("mongo.username");
        String password = properties.getProperty("mongo.password");
        String databaseName = properties.getProperty("mongo.record.dbname");
        try {
            edmServer = new EdmMongoServerImpl(hostURLs, hostPorts, databaseName, username, password);
            LOG.info("Connected to Mongo record database {} on [{}]", databaseName, hostURLs);
        } catch (Exception e) {
            LOG.error("Cannot connect to Mongo record database!", e);
            throw new RuntimeException(e);
        }
    }
    // id: /11601/database_detail_php_ID_187548 ->
    // http://europeana.eu/api/v2/record/11601/database_detail_php_ID_187548.rdf?wskey=...
    public String getRecord(String id) {
        Long start = System.currentTimeMillis();
        String rdf = null;
        try {
            FullBeanImpl fullBean = (FullBeanImpl) edmServer.getFullBean(id);
            if (fullBean != null) {
                rdf = EdmUtils.toEDM(fullBean, false);
            } else {
                LOG.error("No record: {}");
            }
            Long duration = System.currentTimeMillis() - start;
            if (duration > WARN_RECORD_RETRIEVAL_DURATION) {
                LOG.warn("Retrieval of record {} took {}", id, duration);
            }
        } catch (Exception e) {
            //we sometimes get timeoutexceptions here (see EA-912), so print duration to check
            LOG.error("Error retrieving record {}, duration = {}, message = ",id, System.currentTimeMillis() - start, e.getMessage());
            LOG.error("  stacktrace = " + StringUtil.stacktraceAsString(e)); // so we have it in ELK
        }
        return rdf;
    }

    public void close() {
        edmServer.close();
    }

}
