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
import com.ontotext.oai.util.DateConverter;
import com.ontotext.oai.util.SimpleMap;
import org.apache.commons.lang.StringEscapeUtils;

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
//    private DateTimeFormatter dateTimeFormatter = new DateConverter();
//    private final SimpleDateFormat dateFormatter = new SimpleDateFormat();
    DateConverter dateConverter = new DateConverter();
//    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public MongoDbCatalog(Properties properties) {
        if (properties == null) {
            properties = new Properties(); // TODO: log missing file and using defaults
        }

        db = new CommonDb(properties);
        recordsPerPage = Integer.parseInt(properties.getProperty("MongoDbCatalog.recordsPerPage", "100"));
        setsPerPage = Integer.parseInt(properties.getProperty("MongoDbCatalog.setsPerPage", "10"));
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
    public Map listIdentifiers(String from, String until, String set, String metadataPrefix) throws BadArgumentException, CannotDisseminateFormatException, NoItemsMatchException, NoSetHierarchyException, OAIInternalServerError {

        Date dateFrom = null;
        Date dateUntil = null;
        try {
            dateFrom = dateConverter.fromIsoDateTime(from);
            dateUntil = dateConverter.fromIsoDateTime(until);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadArgumentException();
        }
        DBCursor dbCursor = db.listRecords(dateFrom, dateUntil, set);
        ResumptionToken token = new ResumptionToken(dbCursor, id_inc++);
        ResumptionToken oldToken = resumptionMap.get(token.getId());
        if (oldToken != null) {
            token = oldToken;
        } else {
            resumptionMap.put(token.getId(),  token);
        }
        return listIdentifiers(token);
    }

    @Override
    public Map listIdentifiers(String resumptionToken) throws BadResumptionTokenException, OAIInternalServerError {
        ResumptionToken token = resumptionMap.get(resumptionToken);
        if(token == null) {
            throw new BadResumptionTokenException();
        }

        return listIdentifiers(token);
    }

    public Map listIdentifiers(ResumptionToken token) {
        List<String> headers = new ArrayList<String>(recordsPerPage);
        List<String> identifiers = new ArrayList<String>(recordsPerPage);
        Map<String, Object> m = listIdentifiers(token,  headers,  identifiers);
        String[] keys = new String[]{"headers", "identifiers", "resumptionMap"};
        Object[] vals = new Object[] { headers.iterator(), identifiers.iterator(), m};
        return new SimpleMap(keys,  vals);
    }

    private Map<String, Object> listIdentifiers(ResumptionToken token, List<String> headers, List<String> identifiers) {
        Map<String, Object> m = createResumptionMap(token);
        RecordFactory rf = getRecordFactory();
        for (int i = 0; i < recordsPerPage; ++i) {
            if (token.hasNext()) {
                RegistryInfo ri = token.next();
                String header = registryInfo2Xml(ri);
                headers.add(header);
                String identifier = rf.getOAIIdentifier(new RecordInfo(null, ri));
                identifiers.add(identifier);
            } else {
                break;
            }
        }

        if (!token.hasNext()) {
            token.close();
            resumptionMap.remove(token.getId());
            m = null;
        }

        return m;
    }

    Map<String, Object> createResumptionMap(ResumptionToken token) {
        String []keys = {"expirationDate", "cursor", "resumptionToken"};
        String[] values = {dateConverter.toIsoDate(token.getExpirationDate()), Long.toString(token.getCursor()), token.getId()};
        SimpleMap<String, Object> map = new SimpleMap<String, Object>(keys,  values);
        return map;
    }

//    private Map<String, Object> listIdentifiers(DBCursor dbCursor) {
//
//        Iterator<RegistryInfo> dbIterator = new RegistryRecordIterator(dbCursor);
//        HeaderIterator headers = new HeaderIterator(dbIterator);
//        CounterAdapterIterator<String> limitedHeaders = new CounterAdapterIterator<String>(headers,  10);
//        IdentifierIterator identifiers = new IdentifierIterator(dbIterator);
//        CounterAdapterIterator<String> limitedIdentifiers = new CounterAdapterIterator<String>(identifiers,  10);
//        String[] keys = new String[]{"headers", "identifiers"};
//        Object[] vals = new Object[] { limitedHeaders, limitedIdentifiers };
//        Map<String, Object> map = new SimpleMap<String, Object>(keys, vals);

//        HashMap<String, Object> map = new HashMap<String, Object>(5);
//        map.put("headers", limitedHeaders);

//        return map;
//    }

    @Override
    public String getRecord(String identifier, String metadataPrefix) throws IdDoesNotExistException, CannotDisseminateFormatException, OAIInternalServerError {
        RecordFactory rf = getRecordFactory();
        String localIdentifier = rf.fromOAIIdentifier(identifier);
        RegistryInfo registryInfo = db.getRegistryInfo(localIdentifier);
        String xml = removeXmlHeader(db.getRecord(localIdentifier));
        RecordInfo recordInfo = new RecordInfo(xml, registryInfo);
        return constructRecord(recordInfo, metadataPrefix);
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

//    private RecordInfo getRecordInfo(String eid) {
//        String xml = removeXmlHeader(db.getRecord(eid));
//        RegistryInfo ri = db.getRegistryInfo(eid);
//        return new RecordInfo(xml, ri);
//    }

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

    private class HeaderIterator implements Iterator<String> {
        private final Iterator<RegistryInfo> iterator;

        HeaderIterator(Iterator<RegistryInfo> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return registryInfo2Xml(iterator.next());
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
            return getRecordFactory().getOAIIdentifier(new RecordInfo(null, ri));
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

    private String registryInfo2Xml(RegistryInfo registryInfo) {
        StringBuilder sb = new StringBuilder(500);
        if (registryInfo != null) {
            sb.append("<header>");
            RecordFactory recordFactory = getRecordFactory();
            RecordInfo nativeItem = new RecordInfo(null, registryInfo);
            String id = recordFactory.getOAIIdentifier(nativeItem);
            if (id != null) {
                sb.append("<identifier>").append(id).append("</identifier>");
            }

            String datestamp = recordFactory.getDatestamp(nativeItem);
            if (datestamp != null) {
                sb.append("<datestamp>").append(datestamp).append("</datestamp>");
            }

            String setSpec = registryInfo.cid;
            if (setSpec != null) {
                sb.append("<setSpec>").append(setSpec).append("</setSpec>");
            }
            sb.append("</header>");
        }

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();
        AbstractCatalog oaiHandler = new MongoDbCatalog(properties);
        try {
            testListSets(oaiHandler);
//            testListIdentifiers(oaiHandler);
//            Map x = oaiHandler.listRecords("2011-01-01", "2013-01-01", null, "edm");
//            String resumptionToken = (String) x.get("resumptionToken");
//            Iterator identifiers = (Iterator) x.get("identifiers");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            oaiHandler.close();
        }
    }
}
