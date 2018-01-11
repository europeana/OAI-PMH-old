package com.ontotext.oai.europeana.db.solr;

import static com.ontotext.oai.europeana.db.solr.FieldNames.COLLECTION_NAME;
import static com.ontotext.oai.europeana.db.solr.FieldNames.EID;
import static com.ontotext.oai.europeana.db.solr.FieldNames.TIMESTAMP;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.ontotext.oai.util.StringUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import com.ontotext.oai.europeana.db.RecordsRegistry;
import com.ontotext.oai.europeana.db.SetsProvider;

/**
 * Created by Simo on 4.6.2014 г..
 */
public class SolrRegistry implements RecordsRegistry, SetsProvider {
	private static final Log log = LogFactory.getLog(SolrRegistry.class);
	CloudSolrServer server;
	final int rows;

	private RegistryInfoCache cache = new RegistryInfoCache();

	public SolrRegistry(Properties properties) {
		String baseUrls = properties.getProperty("SolrRegistry.URLs", "http://data2.eanadev.org:9191/solr");
		String zookeeperURL = properties.getProperty("SolrRegistry.zookeeperURL", "");
		String solrCore = properties.getProperty("SolrRegistry.core", "search_test");

		LBHttpSolrServer lbTarget;
		try {
			lbTarget = new LBHttpSolrServer(baseUrls.split(","));
		} catch (MalformedURLException e) {
			log.error("Solr Server is not constructed!", e);
			throw new RuntimeException(e);
		}
		server = new CloudSolrServer(zookeeperURL, lbTarget);
		server.setDefaultCollection(solrCore);
		server.connect();

		rows = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "1000"));
	}

	@Override
	public RegistryInfo getRegistryInfo(String recordId) {
		RegistryInfo registryInfo = cache.get(recordId);

		if (registryInfo != null) {
			log.trace("Cached");
			return registryInfo;
		}

		log.debug("Cache miss");

		try {
			SolrQuery query = SolrQueryBuilder.getById(recordId);
			QueryResponse response = server.query(query);
			SolrDocumentList result = response.getResults();
			if (result.size() != 1) {
				log.warn("Record not found: " + recordId);
			} else {
				SolrDocument document = result.get(0);
				registryInfo = toRegistryInfo(document, null);
			}
		} catch (SolrServerException e) {
			log.error("Error executing Solr query (getRegistryInfo): " +e.getMessage());
			log.error("  stacktrace = " + StringUtil.stacktraceAsString(e)); // so we have it in ELK
			throw new RuntimeException(e);
		}

		return registryInfo;
	}

	@Override
	public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String collectionName) {
		SolrQuery query = SolrQueryBuilder.listRecords(from, until, collectionName, rows);
		return cache.add(new QueryIterator(query, collectionName));
	}

	@Override
	public Iterator<DataSet> listSets() {
		SolrQuery query = SolrQueryBuilder.listSets();
		return new FacetIterator(query);
	}

	@Override
	public void close() {

	}

	private static RegistryInfo toRegistryInfo(SolrDocument document, String collectionName) {
		String cid = null;
		if (collectionName != null) {
			cid = collectionName;
		} else {
			ArrayList<String> arr = (ArrayList<String>) document.getFieldValue(COLLECTION_NAME);
			if (!arr.isEmpty()) {
				cid = arr.get(0);
				cid = StringEscapeUtils.escapeXml(cid);
			} else {
				log.error("Collection name is missing!");
			}
		}
		String eid = (String) document.getFieldValue(EID);
		Date timestamp = (Date) document.getFieldValue(TIMESTAMP);
		final boolean deleted = false;

		return new RegistryInfo(cid, eid, timestamp, deleted);
	}

	protected class QueryIterator implements CloseableIterator<RegistryInfo> {
		private final SolrQuery query;
		private String cursorMark = SolrHelper.CURSOR_MARK_START;
		private final String fixed_cid; // used to reduce result fields when
										// query has collectionId filter
		SolrDocumentList resultList;
		int currentIndex;
		private RegistryInfo last;

		public QueryIterator(SolrQuery query, String cid) {
			this.query = query;
			this.fixed_cid = StringEscapeUtils.escapeXml(cid);
			fetch();
		}

		@Override
		public void close() {
		}

		@Override
		public boolean hasNext() {
			if (currentIndex < resultList.size()) {
				return true;
			}

			return fetch();
		}

		@Override
		public RegistryInfo next() {
			SolrDocument document = resultList.get(currentIndex++);
			last = toRegistryInfo(document, fixed_cid);
			return last;
		}

		@Override
		public void remove() {

		}

		public RegistryInfo last() {
			return last;
		}

		private boolean fetch() {
			if (cursorMark != null) {
				SolrHelper.setCursorMark(query, cursorMark);
				log.trace("Cursor mark: " + cursorMark);

				try {
					log.trace("Getting more records");
					QueryResponse response = server.query(query);
					log.trace("Getting more records finished.");
					String nextCursorMark = SolrHelper.getNextCursorMark(response);

					cursorMark = (cursorMark.equals(nextCursorMark)) ? null : nextCursorMark;

					resultList = response.getResults();
					currentIndex = 0;
					return resultList.size() != 0;
				} catch (SolrServerException e) {
					log.error("Error executing Solr query (fetch): " +e.getMessage());
					log.error("  stacktrace = " + StringUtil.stacktraceAsString(e)); // so we have it in ELK
					throw new RuntimeException(e);
				}

			}

			return false;
		}
	}

	private class FacetIterator implements CloseableIterator<DataSet> {

		private final SolrQuery query;
		private List<FacetField.Count> names;
		int currentIndex;
		int offset = 0;

		public FacetIterator(SolrQuery query) {
			this.query = query;
			getMore();
		}

		@Override
		public void close() {

		}

		@Override
		public boolean hasNext() {
			if (names != null && currentIndex < names.size()) {
				return true;
			}

			return getMore();
		}

		@Override
		public DataSet next() {
			FacetField.Count nameCount = names.get(currentIndex++);
			String name = nameCount.getName(); // escapeXml in dataSet2Xml
			return new DataSet(name, null); // name is id here
		}

		@Override
		public void remove() {

		}

		private boolean getMore() {
			SolrHelper.setFacetOffset(query, offset);
			offset += query.getFacetLimit();
			try {
				QueryResponse response = server.query(query);
				FacetField collectionNames = response.getFacetField(COLLECTION_NAME);

				currentIndex = 0;
				names = collectionNames.getValues();
				return !names.isEmpty();
			} catch (SolrServerException e) {
				log.error("Error executing Solr query (getMore): " +e.getMessage());
				log.error("  stacktrace = " + StringUtil.stacktraceAsString(e)); // so we have it in ELK
				throw new RuntimeException(e);
			}
		}
	}

}
