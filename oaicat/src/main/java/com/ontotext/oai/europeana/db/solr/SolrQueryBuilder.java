package com.ontotext.oai.europeana.db.solr;

import com.ontotext.oai.util.DateConverter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;

import java.util.Date;

import static com.ontotext.oai.europeana.db.solr.FieldNames.*;

/**
 * Created by Simo on 6.6.2014 г..
 */
public class SolrQueryBuilder {
    private static DateConverter dateConverter = new DateConverter();
    public static SolrQuery listRecords(Date from, Date until, String collectionName, int rows) {
//        SolrQuery query = new SolrQuery(listRecordsQ(from, until));
        SolrQuery query = new SolrQuery("*:*");
        setFilter(query, collectionName,  from,  until);
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
        query.setFields(COLLECTION_NAME);
        query.addFacetField(COLLECTION_NAME);
        query.setFacet(true);
        query.setFacetLimit(facetLimit);

        return query;
    }

    public static SolrQuery setFacetOffset(SolrQuery query, int offset) {
        query.set(FacetParams.FACET_OFFSET, offset);
        return query;
    }

    public static void setFilter(SolrQuery query, String set, Date fromDate, Date untilDate) {
        int size = (set == null) ? 1 : 2;
        int index = 0;
        String[] filters = new String[size];
        if (set != null) {
            filters[index++] = (COLLECTION_NAME + ":\"" + set + '"');
        }

        if (fromDate != null) {
            String from = DateConverter.toIsoDate2(fromDate);
            String until = DateConverter.toIsoDate2(untilDate);
            filters[index] = (FieldNames.TIMESTAMP + ":[" + from + " TO " + until + "]");
        }

        query.set(CommonParams.FQ, filters);
    }
}
