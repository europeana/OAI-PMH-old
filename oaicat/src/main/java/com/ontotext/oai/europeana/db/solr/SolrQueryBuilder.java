package com.ontotext.oai.europeana.db.solr;

import org.apache.solr.client.solrj.SolrQuery;

import static com.ontotext.oai.europeana.db.solr.FieldNames.COLLECTION_NAME;
import static com.ontotext.oai.europeana.db.solr.FieldNames.EID;
import static com.ontotext.oai.europeana.db.solr.FieldNames.TIMESTAMP;

/**
 * Created by Simo on 6.6.2014 г..
 */
public class SolrQueryBuilder {
    public static SolrQuery listRecords(String from, String until, String collectionName, int rows) {
        SolrQuery query = new SolrQuery(listRecordsQ(collectionName, from, until));
        query.setRows(rows);

        query.addField(TIMESTAMP);
        query.addField(EID);
        if (collectionName == null) {
            query.addField(COLLECTION_NAME);
        }

        query.addSortField(TIMESTAMP, SolrQuery.ORDER.asc);
        query.addSortField(EID, SolrQuery.ORDER.asc);

        return query;
    }

    public static SolrQuery getById(String eid) {
        SolrQuery query = new SolrQuery(EID + ":\"" + eid + '"');
        query.addField(TIMESTAMP);
        query.addField(EID);
        query.addField(COLLECTION_NAME);

        return query;
    }

    private static String listRecordsQ(String collectionName, String from, String until) {
        if (collectionName == null && from == null && until == null) {
            return "*:*";
        }

        StringBuilder q = new StringBuilder(200);
        boolean limitByDate = from != null || until != null;
        if (limitByDate) {
            q.append(TIMESTAMP).append(":[");
            q.append(from == null ? "*" : from);
            q.append(" TO ");
            q.append(until == null ? "*" : until);
            q.append(']');
        }

        if (collectionName != null) {
            if (limitByDate) {
                q.append(" AND ");
            }

            q.append(COLLECTION_NAME).append(":\"").append(collectionName).append('"');
        }

        return q.toString();
    }
}
