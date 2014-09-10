package com.ontotext.oai.europeana.db.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.FacetParams;

/**
 * Created by Simo on 22.8.2014 г..
 */
public class SolrHelper {
    public static final String CURSOR_MARK_PARAM = "cursorMark";
    public static final String CURSOR_MARK_NEXT = "nextCursorMark";
    public static final String CURSOR_MARK_START = "*";

    //    public static int getFacetOffset(SolrQuery query) {
//        return Integer.parseInt(query.get(FacetParams.FACET_OFFSET));
//    }

    public static SolrQuery setFacetOffset(SolrQuery query, int offset) {
        query.set(FacetParams.FACET_OFFSET, offset);
        return query;
    }

    public static SolrQuery setCursorMark(SolrQuery query, String cursorMark) {
        query.set(CURSOR_MARK_PARAM, cursorMark);
        return query;
    }

    public static String getNextCursorMark(QueryResponse response) {
        return (String)response.getResponse().get(CURSOR_MARK_NEXT);
    }
}
