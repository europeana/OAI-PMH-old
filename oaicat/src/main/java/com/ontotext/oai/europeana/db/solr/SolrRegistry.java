package com.ontotext.oai.europeana.db.solr;

import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import com.ontotext.oai.europeana.db.RecordsRegistry;
import com.ontotext.oai.util.DateConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import static com.ontotext.oai.europeana.db.solr.FieldNames.*;

/**
 * Created by Simo on 4.6.2014 г..
 */
public class SolrRegistry implements RecordsRegistry {
    private static final Log log = LogFactory.getLog(SolrRegistry.class);
    DateConverter dateConverter = new DateConverter();
    HttpSolrServer server;
    final int rows;
    RegistryInfo cachedRegistryInfo = null;

    public SolrRegistry(Properties properties) {
        String baseUrl = properties.getProperty("SolrRegistry.server", "http://data2.eanadev.org:9191/solr");
        server = new HttpSolrServer(baseUrl);
        rows = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "100"));
    }

    @Override
    public RegistryInfo getRegistryInfo(String recordId) {
        if (cachedRegistryInfo != null) {
            if (recordId.equals(cachedRegistryInfo.eid)) {
                return cachedRegistryInfo;
            }
            cachedRegistryInfo = null; // clear cache
        }

        try {
            SolrQuery query = SolrQueryBuilder.getById(recordId);
            QueryResponse response = server.query(query);
            SolrDocumentList result = response.getResults();
            if  (result.size() != 1) {
                log.info("Record not found: " + recordId);
            } else {
                SolrDocument document = result.get(0);
                cachedRegistryInfo = toRegistryInfo(document, null);
            }
        } catch (SolrServerException e) {
            log.fatal("Error executing Solr query", e);
            return null;
        }

        return cachedRegistryInfo;
    }

    @Override
    public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String collectionName) {
        SolrQuery query = SolrQueryBuilder.listRecords(
                dateConverter.toIsoDate(from), dateConverter.toIsoDate(until),
                collectionName, rows);
        return new QueryIterator(query, collectionName);
    }

    @Override
    public void close() {

    }

    private static RegistryInfo toRegistryInfo(SolrDocument document, String collectionName) {
        String cid = null;
        if (collectionName != null) {
            cid = collectionName;
        } else {
            ArrayList<String> arr = (ArrayList<String>)document.getFieldValue(COLLECTION_NAME);
            if (!arr.isEmpty()) {
                cid = arr.get(0);
            } else {
                log.fatal("Collection name is missing!");
            }
        }
        String eid = (String) document.getFieldValue(EID);
        Date timestamp = (Date) document.getFieldValue(TIMESTAMP);
        final boolean deleted = false;

        return new RegistryInfo(cid,  eid,  timestamp,  deleted);
    }

    private class QueryIterator implements CloseableIterator<RegistryInfo> {
        private final SolrQuery query;
        private final String fixed_cid; // used to reduce result fields when query has collectionId filter
        SolrDocumentList resultList;
        int currentIndex;

        public QueryIterator(SolrQuery query, String cid) {
            this.query = query;
            this.fixed_cid = cid;
            getMore(0);
        }

        @Override
        public void close() {
        }

        @Override
        public boolean hasNext() {
            if (currentIndex < resultList.size()) {
                return true;
            }

            return getMore(query.getStart() + query.getRows());
        }

        @Override
        public RegistryInfo next() {
            SolrDocument document = resultList.get(currentIndex++);
            cachedRegistryInfo = toRegistryInfo(document, fixed_cid);
            return cachedRegistryInfo;
        }

        @Override
        public void remove() {

        }

        private boolean getMore(int start) {
            query.setStart(start);

            try {
                log.trace("Getting more records");
                QueryResponse response = server.query(query);
                log.trace("Getting more records finished.");
                resultList = response.getResults();
                currentIndex = 0;
                return resultList.size() != 0;
            } catch (SolrServerException e) {
                log.fatal("Error executing Solr query", e);
            }

            return false;
        }
    }
}
