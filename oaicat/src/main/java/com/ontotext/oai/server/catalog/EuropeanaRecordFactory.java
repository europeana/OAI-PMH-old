package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.util.DateConverter;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:29
 */

public class EuropeanaRecordFactory extends RecordFactory {
//    private static Log logger = LogFactory.getLog(EuropeanaRecordFactory.class);
    String repositoryId = "europeana.eu"; // may be moved in property
    private final DateConverter dateConverter = new DateConverter();

    public EuropeanaRecordFactory(Properties properties) {
        super(properties);
    }

    @Override
    public String fromOAIIdentifier(String identifier) {
        // oai:europeana.eu:ds:id-> /ds/id
        StringTokenizer tokenizer = new StringTokenizer(identifier,  ":");
        tokenizer.nextToken(); // skip "oai:"
        tokenizer.nextToken(); // skip "europeana.eu:"
        return "/" + tokenizer.nextToken() + "/" + tokenizer.nextToken();
    }

    @Override
    public String quickCreate(Object nativeItem, String schemaURL, String metadataPrefix) throws IllegalArgumentException, CannotDisseminateFormatException {
        return null; // not supported
    }

    @Override
    public String getOAIIdentifier(Object nativeItem) {
        String localIdentifier = asRecord(nativeItem).eid;

        if (localIdentifier != null) {
            // oai:europeana.eu:ds:id-> /ds/id
            StringTokenizer tokenizer = new StringTokenizer(localIdentifier, "/");
//            tokenizer.nextToken();// no need to skip leading slash (strange logic)

            return "oai:" + repositoryId + ":" + tokenizer.nextToken() + ":" + tokenizer.nextToken() ;
        }

        return null;
    }

    @Override
    public String getDatestamp(Object nativeItem) {
        Date timeStamp = asRecord(nativeItem).last_checked;
        if (timeStamp != null) {
            return dateConverter.toIsoDate(timeStamp);
        }
        return null;
    }

    @Override
    public Iterator getSetSpecs(Object nativeItem) throws IllegalArgumentException {
        String setSpec = asRecord(nativeItem).cid;
        if (setSpec != null) {
            return Arrays.asList(new String[] {setSpec}).iterator();
        }
        return null;
    }

    @Override
    public boolean isDeleted(Object nativeItem) {
        return asRecord(nativeItem).deleted;
    }

    @Override
    public Iterator getAbouts(Object nativeItem) {
        return null;
    }

    private static RegistryInfo asRecord(Object nativeItem) throws IllegalArgumentException {
        if (nativeItem instanceof RegistryInfo) {
            return (RegistryInfo) nativeItem;
        }

        throw new IllegalArgumentException();
    }
}
