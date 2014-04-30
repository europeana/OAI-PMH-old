package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.*;
import com.mongodb.DBCursor;
import com.ontotext.oai.RecordInfo;
import com.ontotext.oai.ResumptionToken;
import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CommonDb;
import com.ontotext.oai.server.iterator.HeadersIterator;
import com.ontotext.oai.server.iterator.IdentifiersIterator;
import com.ontotext.oai.util.Callback;
import com.ontotext.oai.util.DateConverter;
import com.ontotext.oai.util.NullifyObjectCallback;
import com.ontotext.oai.util.SimpleMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:24
 */
public class MongoDbCatalog extends AbstractCatalog {
    private CommonDb db;
    private Map<String, ResumptionToken> resumptionMap = new ConcurrentHashMap<String, ResumptionToken>();
    private long id_inc = 0;
    private final int recordsPerPage;
    private final int setsPerPage;
    private final Log log = LogFactory.getLog(MongoDbCatalog.class);
    private static final long CLEANUP_MINUTES = 1L;
    private static final long CLEANUP_MILLISECONDS = CLEANUP_MINUTES*60L*1000L;
//    private DateTimeFormatter dateTimeFormatter = new DateConverter();
//    private final SimpleDateFormat dateFormatter = new SimpleDateFormat();
    DateConverter dateConverter = new DateConverter();
//    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private boolean debug;

    public MongoDbCatalog(Properties properties) {
        if (properties == null) {
            properties = new Properties(); // TODO: log missing file and using defaults
        }

        db = new CommonDb(properties);
        recordsPerPage = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "100"));
        setsPerPage = Integer.parseInt(properties.getProperty("MongoDbCatalog.setsPerPage", "10"));
        debug = Boolean.parseBoolean(properties.getProperty("MongoDbCatalog.debug", "false"));
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int numTokens = resumptionMap.size();
                if (numTokens == 0) {
                    return;
                }
                log.debug("Cleanup thread. Num tokens: " + numTokens);
                Date now = new Date();
                for (Iterator<Map.Entry<String,ResumptionToken>> iterator = resumptionMap.entrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<String, ResumptionToken> entry = iterator.next();
                    ResumptionToken token = entry.getValue();
                    Date expireDate = token.getExpirationDate();
                    if (now.after(expireDate)) {
                        log.info("Remove token: " + token.getId());
                        iterator.remove();
                        token.close();
                    }
                }
            }
        }, CLEANUP_MILLISECONDS, CLEANUP_MILLISECONDS);
    }

    @Override
    public Map listSets() throws NoSetHierarchyException, OAIInternalServerError {
        List<DataSet> europeanaCollections = db.listSets();
        Map <String, Object> m = new HashMap<String, Object>();
        m.put("sets", new XmlDataSetIterator(europeanaCollections.iterator()));

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
        if (debug) {
            log.info("listRecords4(" + from + ", " + until + ", " + set + ", " + metadataPrefix + ")");
        }
        Map listRecordsMap = super.listRecords(from, until, set, metadataPrefix);
        addResumptionMap(listRecordsMap);
        return listRecordsMap;
    }

    @Override
    public Map listRecords(String resumptionToken)
            throws BadResumptionTokenException, OAIInternalServerError {
        if (debug) {
            log.info("listRecords1(" + resumptionToken + ")");
        }
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
    public Map listIdentifiers(String from, String until, String set, String metadataPrefix) throws BadArgumentException, CannotDisseminateFormatException, NoItemsMatchException, NoSetHierarchyException, OAIInternalServerError {

        Date dateFrom = null;
        Date dateUntil = null;
        try {
            dateFrom = dateConverter.fromIsoDateTime(from);
            dateUntil = dateConverter.fromIsoDateTime(until);
        } catch (Exception e) {
            log.error(e);
            throw new BadArgumentException();
        }
        DBCursor dbCursor = db.listRecords(dateFrom, dateUntil, set).batchSize(recordsPerPage);
        ResumptionToken token = new ResumptionToken(dbCursor, id_inc++);
        ResumptionToken oldToken = resumptionMap.get(token.getId());
        if (oldToken != null) {
            token = oldToken;
            log.error("Duplicate tokenId: " + token.getId());
        } else {
            resumptionMap.put(token.getId(),  token);
            log.info("Add token: " + token.getId());
        }
        return listIdentifiers(token);
    }

    @Override
    public Map listIdentifiers(String resumptionToken) throws BadResumptionTokenException, OAIInternalServerError {
        ResumptionToken token = resumptionMap.get(resumptionToken);
        if(token == null) {
            log.error("Unknown resumption token");
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
        String[] values = {dateConverter.toIsoDate(token.getExpirationDate()), Long.toString(token.getCursor()), token.getId()};
        SimpleMap<String, Object> map = new SimpleMap<String, Object>(keys,  values);
        return map;
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
        else if (debug) {
            String fullXml = db.getRecord(localIdentifier);
            if (fullXml != null) {
                log.warn("Record exists, but no registry entry: " + localIdentifier);
            } else {
                log.warn("No registry entry for record: " + localIdentifier);
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

    private static void testListSets(AbstractCatalog oaiHandler) throws NoSetHierarchyException, OAIInternalServerError {
        oaiHandler.listSets();
    }

    private static void testListIdentifiers(AbstractCatalog oaiHandler) throws CannotDisseminateFormatException, NoSetHierarchyException, OAIInternalServerError, BadArgumentException, NoItemsMatchException {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2013, Calendar.SEPTEMBER, 1);
//        String from  = calendar.getTime().toString();
//        calendar.add(Calendar.MONTH, 1);
//        String until  = calendar.getTime().toString();
        String from = "2013-09-01T00:00Z";
        String until = "2013-10-01T00:00Z";
        oaiHandler.listIdentifiers(from, until, null, "edm");
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        FileInputStream propertiesStream = new FileInputStream("europeana.properties");
        try {
            properties.load(propertiesStream);
        } finally {
            propertiesStream.close();
        }

        return properties;
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

    private class IdentifierIterator implements Iterator<String> {
        private final Iterator<RegistryInfo> iterator;

        IdentifierIterator(Iterator<RegistryInfo> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            RegistryInfo ri = iterator.next();
            return getRecordFactory().getOAIIdentifier(ri);
        }

        @Override
        public void remove() {

        }
    }

    private String dataSet2Xml(DataSet ds) {
        RecordFactory rf = getRecordFactory();
//        rf.createHeader();
        StringBuilder sb = new StringBuilder(200);
        if (ds != null) {
            sb.append("<set>");
            if (ds.identifier != null) {
                sb.append("<setSpec>").append(ds.identifier).append("</setSpec>");
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
