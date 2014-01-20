package com.ontotext.oai.server.catalog;

import ORG.oclc.oai.server.catalog.RecordFactory;
import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.ontotext.oai.RecordInfo;
import com.ontotext.oai.server.crosswalk.Edm2Provenance;
import com.ontotext.oai.server.crosswalk.Edm2Rights;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-12-9
 * Time: 11:29
 */

public class EuropeanaRecordFactory extends RecordFactory {
//    private static Log logger = LogFactory.getLog(EuropeanaRecordFactory.class);
    public EuropeanaRecordFactory(Properties properties) {
        super(properties);
    }

    @Override
    public String fromOAIIdentifier(String identifier) {
        // oai:ds:id-> /ds/id
        StringTokenizer tokenizer = new StringTokenizer(identifier,  ":");
        tokenizer.nextToken(); // skip "oai:"
        return "/" + tokenizer.nextToken() + "/" + tokenizer.nextToken();
    }

    @Override
    public String quickCreate(Object nativeItem, String schemaURL, String metadataPrefix) throws IllegalArgumentException, CannotDisseminateFormatException {
        return null; // not supported
    }

    @Override
    public String getOAIIdentifier(Object nativeItem) {
        String localIdentifier = asRecord(nativeItem).getLocalId();

        if (localIdentifier != null) {
            // oai:ds:id-> /ds/id
            StringTokenizer tokenizer = new StringTokenizer(localIdentifier, "/");
//            tokenizer.nextToken();// no need to skip leading slash (strange logic)

            return "oai:" + tokenizer.nextToken() + ":" + tokenizer.nextToken() ;
        }

        return null;
    }

    @Override
    public String getDatestamp(Object nativeItem) {
        Date timeStamp = asRecord(nativeItem).getTimeStamp();
        if (timeStamp != null) {
            return timeStamp.toString();
        }
        return null;
    }

    @Override
    public Iterator getSetSpecs(Object nativeItem) throws IllegalArgumentException {
        String setSpec = asRecord(nativeItem).getSetId();
        if (setSpec != null) {
            return Arrays.asList(new String[] {setSpec}).iterator();
        }
        return null;
    }

    @Override
    public boolean isDeleted(Object nativeItem) {
        return asRecord(nativeItem).isDeleted();
    }

    @Override
    public Iterator getAbouts(Object nativeItem) {
        RecordInfo recordInfo = asRecord(nativeItem);
        List<String> abouts = new ArrayList<String>();
        if (recordInfo != null) {
            try {
                Crosswalk cws[] = new Crosswalk[] {new Edm2Provenance(), new Edm2Rights()};
                for (Crosswalk cw : cws) {
                    String about = getAbout(recordInfo, cw);
                    if (about != null) {
                        abouts.add(about);
                    }
                }
            } catch (CannotDisseminateFormatException e) {
                e.printStackTrace();
            }
        }
        return abouts.iterator();
    }

    private String getAbout(RecordInfo recordInfo, Crosswalk cw) throws CannotDisseminateFormatException {
        String about = null;
        if (cw.isAvailableFor(recordInfo)) {
            about = cw.createMetadata(recordInfo);
        }

        return about;
    }

    private static RecordInfo asRecord(Object nativeItem) throws IllegalArgumentException {
        if (nativeItem instanceof RecordInfo) {
            return (RecordInfo) nativeItem;
        }

        throw new IllegalArgumentException();
    }
}
