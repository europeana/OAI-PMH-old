package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.*;
import com.ontotext.oai.RecordInfo;
import com.ontotext.oai.ResumptionToken;
import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import com.ontotext.oai.europeana.db.CommonDb;
import com.ontotext.oai.server.iterator.HeadersIterator;
import com.ontotext.oai.server.iterator.IdentifiersIterator;
import com.ontotext.oai.util.Callback;
import com.ontotext.oai.util.DateConverter;
import com.ontotext.oai.util.NullifyObjectCallback;
import com.ontotext.oai.util.SimpleMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:24
 */
public class MongoDbCatalog extends AbstractCatalog {

    private static final Logger LOG = LogManager.getLogger(MongoDbCatalog.class);
    private static final long CLEANUP_TOKEN_MINUTES = 1L;
    private static final long CLEANUP_TOKEN_MILLISECONDS = CLEANUP_TOKEN_MINUTES*60L*1000L;

    private final int recordsPerPage;

    private CommonDb db;
    private Map<String, ResumptionToken> resumptionMap = new ConcurrentHashMap<>();
    private long id_inc = 0;


    public MongoDbCatalog(Properties properties) {
        db = new CommonDb(properties);
        recordsPerPage = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "100"));
        LOG.info("Records per page: {}", recordsPerPage);
        scheduleCleanupThread();
    }

    /**
     * Create daemon thread to check every minute for expired tokens and remove them from map.
     */
    private void scheduleCleanupThread() {
        Timer timer = new Timer(true); // isDaemon
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int numTokens = resumptionMap.size();
                if (numTokens == 0) {
                    return;
                }
                LOG.debug("Cleanup thread. Num tokens: {}", numTokens);
                Date now = new Date();
                for (Iterator<Map.Entry<String,ResumptionToken>> iterator = resumptionMap.entrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<String, ResumptionToken> entry = iterator.next();
                    ResumptionToken token = entry.getValue();
                    Date expireDate = token.getExpirationDate();
                    if (now.after(expireDate)) {
                        LOG.info("Remove token: {}", token.getId());
                        iterator.remove();
                        token.close();
                    }
                }
            }
        }, CLEANUP_TOKEN_MILLISECONDS, CLEANUP_TOKEN_MILLISECONDS);
    }

    @Override
    public Map listSets() throws NoSetHierarchyException, OAIInternalServerError {
        Iterator<DataSet> europeanaCollections = db.listSets();
        Map <String, Object> m = new HashMap<>();
        m.put("sets", new XmlDataSetIterator(europeanaCollections));

        return m;
    }

    @Override
    public Map listSets(String resumptionToken) throws BadResumptionTokenException, OAIInternalServerError {
        throw new BadResumptionTokenException(); // not implemented
    }

    @Override
    public Vector getSchemaLocations(String identifier) throws IdDoesNotExistException, NoMetadataFormatsException, OAIInternalServerError {
        return null;  // Nothing to do. Works with the basic functionality.
    }

    @Override
    public Map listRecords(String from, String until, String set, String metadataPrefix)
            throws BadArgumentException, CannotDisseminateFormatException, NoItemsMatchException,
            NoSetHierarchyException, OAIInternalServerError {
        LOG.debug("listRecords4({}, {}, {}, {})", from, until, set, metadataPrefix);
        Map listRecordsMap = super.listRecords(from, until, set, metadataPrefix);
        addResumptionMap(listRecordsMap);
        return listRecordsMap;
    }

    @Override
    public Map listRecords(String resumptionToken)
            throws BadResumptionTokenException, OAIInternalServerError {
        LOG.debug("listRecords1({})", resumptionToken);
        Map listRecordsMap = super.listRecords(resumptionToken);
        addResumptionMap(listRecordsMap);
        return listRecordsMap;
    }

    private void addResumptionMap(Map listRecordsMap) {
        String resumptionToken = (String)listRecordsMap.get("resumptionToken");
        if (resumptionToken == null) {
            return;
        }
        ResumptionToken token = resumptionMap.get(resumptionToken);
        if (token != null) {
            Map<String, Object> m = createResumptionMap(token);
            listRecordsMap.put("resumptionMap", m);
        }
    }


    @Override
    public Map listIdentifiers(String from, String until, String set, String metadataPrefix) throws BadArgumentException, CannotDisseminateFormatException,
            NoItemsMatchException, NoSetHierarchyException, OAIInternalServerError {
        Date dateFrom;
        Date dateUntil;
        try {
            dateFrom = DateConverter.fromIsoDateTime(from);
            dateUntil = DateConverter.fromIsoDateTime(until);
        } catch (Exception e) {
            LOG.error(e);
            throw new BadArgumentException();
        }
        CloseableIterator<RegistryInfo> dbCursor = db.listRecords(dateFrom, dateUntil, set);
        ResumptionToken token = new ResumptionToken(dbCursor, id_inc++);
        ResumptionToken oldToken = resumptionMap.get(token.getId());
        if (oldToken != null) {
            token = oldToken;
            LOG.error("Duplicate tokenId: {}", token.getId());
        } else {
            resumptionMap.put(token.getId(), token);
            LOG.info("Add token: {}", token.getId());
        }
        return listIdentifiers(token);
    }

    @Override
    public Map listIdentifiers(String resumptionToken) throws BadResumptionTokenException, OAIInternalServerError {
        ResumptionToken token = resumptionMap.get(resumptionToken);
        if(token == null) {
            LOG.error("Unknown resumption token");
            throw new BadResumptionTokenException();
        }

        return listIdentifiers(token);
    }

    public Map listIdentifiers(ResumptionToken token) {
        RecordFactory recordFactory = getRecordFactory();
        // Both iterators are alternatively used, but here I don't know which to put in the map.
        // Headers are used only in ListIdentifiers; iterators are used in ListRecords.
        HeadersIterator headers = new HeadersIterator(token, recordFactory, recordsPerPage);
        IdentifiersIterator identifiers =  new IdentifiersIterator(token, recordFactory, recordsPerPage);
        Map<String, Object> m = createResumptionMap(token);
        String[] keys = new String[]{"headers", "identifiers", "resumptionMap", "resumptionToken"};
        Object[] vals = new Object[] { headers, identifiers, m, token.getId()};
        Callback removeResumptionMap = new NullifyObjectCallback(vals, new int[]{2, 3} );
        headers.setCallback(removeResumptionMap);
        identifiers.setCallback(removeResumptionMap);
        return new SimpleMap<String, Object>(keys,  vals);
    }

    Map<String, Object> createResumptionMap(ResumptionToken token) {
        String []keys = {"expirationDate", "cursor", "resumptionToken"};
        String[] values = {DateConverter.toIsoDate(token.getExpirationDate()), Long.toString(token.getCursor()), token.getId()};
        return new SimpleMap<String, Object>(keys,  values);
    }

    @Override
    public String getRecord(String identifier, String metadataPrefix) throws IdDoesNotExistException, CannotDisseminateFormatException, OAIInternalServerError {
        String record = null;
        RecordFactory rf = getRecordFactory();
        String localIdentifier = rf.fromOAIIdentifier(identifier);
        RegistryInfo registryInfo = db.getRegistryInfo(localIdentifier);
        if (registryInfo != null) {
            String xml = removeXmlHeader(db.getRecord(localIdentifier));
            RecordInfo recordInfo = new RecordInfo(xml, registryInfo);
            record = constructRecord(recordInfo, metadataPrefix);
        }
        else if (LOG.isDebugEnabled()) {
            String fullXml = db.getRecord(localIdentifier);
            if (fullXml != null) {
                LOG.warn("Record exists, but no registry entry: {}", localIdentifier);
            } else {
                LOG.warn("No registry entry for record: {}", localIdentifier);
            }
        }

        return record;
    }

    @Override
    public void close() {
        if (db != null) {
            db.close();
        }
    }

    private String constructRecord(RecordInfo nativeItem, String metadataPrefix)
    throws CannotDisseminateFormatException, OAIInternalServerError {
        String schemaURL = null;
        Iterator setSpecs = null; //getSetSpecs(nativeItem);
        Iterator abouts = null; //getAbouts(nativeItem);

        if (metadataPrefix != null) {
            if ((schemaURL = getCrosswalks().getSchemaURL(metadataPrefix)) == null)
                throw new CannotDisseminateFormatException(metadataPrefix);
        }
        return getRecordFactory().create(nativeItem, schemaURL, metadataPrefix, setSpecs, abouts);
    }

    // remove xml prefix <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    private static String removeXmlHeader(String xml) {
        if (xml != null) {
            int pos = xml.indexOf("?>");
            if (pos >= 0) {
                xml = xml.substring(pos + 2);
            }
        }

        return xml;
    }

    private class XmlDataSetIterator implements Iterator<String> {
        private final Iterator<DataSet> iterator;
        XmlDataSetIterator(Iterator<DataSet> iterator) { this.iterator = iterator; }
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
        @Override
        public String next() {
            return dataSet2Xml(iterator.next());
        }
        @Override
        public void remove() {}
    }

    private String dataSet2Xml(DataSet ds) {
        StringBuilder sb = new StringBuilder(200);
        if (ds != null) {
            sb.append("<set>");
            if (ds.identifier != null) {
                String setId = StringEscapeUtils.escapeXml(ds.identifier);
                sb.append("<setSpec>").append(setId).append("</setSpec>");
            }
            if (ds.name != null) {
                String setName = StringEscapeUtils.escapeXml(ds.name);
                sb.append("<setName>").append(setName).append("</setName>");
            }
            sb.append("</set>");
        }

        return  sb.toString();
    }
}
