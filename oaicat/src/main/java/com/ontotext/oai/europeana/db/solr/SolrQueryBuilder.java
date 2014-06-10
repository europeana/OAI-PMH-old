package com.ontotext.oai.europeana.db.solr;

import com.ontotext.oai.util.DateConverter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;

import java.util.Date;

import static com.ontotext.oai.europeana.db.solr.FieldNames.COLLECTION_NAME;
import static com.ontotext.oai.europeana.db.solr.FieldNames.EID;
import static com.ontotext.oai.europeana.db.solr.FieldNames.TIMESTAMP;

/**
 * Created by Simo on 6.6.2014 г..
 */
public class SolrQueryBuilder {
    private static DateConverter dateConverter = new DateConverter();
    public static SolrQuery listRecords(Date from, Date until, String collectionName, int rows) {
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

    public static SolrQuery listSets() {
        int facetLimit = 1000;
        SolrQuery query = new SolrQuery("*:*");
        query.setFields("europeana_collectionName");
        query.addFacetField("europeana_collectionName");
        query.setFacet(true);
        query.setFacetLimit(facetLimit);

        return query;
    }

    public static SolrQuery setFacetOffset(SolrQuery query, int offset) {
        query.set(FacetParams.FACET_OFFSET, offset);
        return query;
    }

    public int getFacetOffset(SolrQuery query) {
        return Integer.parseInt(query.get(FacetParams.FACET_OFFSET));
    }
    private static String listRecordsQ(String collectionName, Date fromDate, Date untilDate) {
        if (collectionName == null && fromDate == null && untilDate == null) {
            return "*:*";
        }

        StringBuilder q = new StringBuilder(200);
        boolean limitByDate = fromDate != null || untilDate != null;
        if (limitByDate) {
            String from = dateConverter.toIsoDate(fromDate);
            String until = dateConverter.toIsoDate(new Date(untilDate.getTime() + 1000L));
            q.append(TIMESTAMP).append(":[");
            q.append(from == null ? "*" : from);
            q.append(" TO ");
            q.append(until == null ? "*" : until);
            q.append('}');
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
