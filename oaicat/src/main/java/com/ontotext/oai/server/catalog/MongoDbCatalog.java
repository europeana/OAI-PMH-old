package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.*;
import com.ontotext.oai.RecordInfo;
import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CommonDb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:24
 */
public class MongoDbCatalog extends AbstractCatalog {
    CommonDb db;
    private final int recordsPerPage;
    private final int setsPerPage;

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
        m.put("sets", europeanaCollections.iterator());

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map listIdentifiers(String resumptionToken) throws BadResumptionTokenException, OAIInternalServerError {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

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

    private RecordInfo getRecordInfo(String eid) {
        String xml = removeXmlHeader(db.getRecord(eid));
        RegistryInfo ri = db.getRegistryInfo(eid);
        return new RecordInfo(xml, ri);
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

    private static void testListIdentifiers() {

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

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();

        AbstractCatalog oaiHandler = new MongoDbCatalog(properties);
        try {
            testListSets(oaiHandler);
//            Map x = oaiHandler.listRecords("2011-01-01", "2013-01-01", null, "edm");
//            String resumptionToken = (String) x.get("resumptionToken");
//            Iterator identifiers = (Iterator) x.get("identifiers");
        } catch (NoSetHierarchyException e) {
            e.printStackTrace();
        } catch (OAIInternalServerError oaiInternalServerError) {
            oaiInternalServerError.printStackTrace();
        } finally {
            oaiHandler.close();
        }
    }
}
