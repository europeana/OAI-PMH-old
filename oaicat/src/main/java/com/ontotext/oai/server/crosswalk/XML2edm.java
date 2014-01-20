package com.ontotext.oai.server.crosswalk;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.ontotext.oai.RecordInfo;

import java.util.Properties;

/**
 * Created by Simo on 13-12-11.
 */
public class XML2edm extends Crosswalk {
    private final static String schemaLocation =
            "http://www.europeana.eu/schemas/edm/ http://www.europeana.eu/schemas/edm/EDM.xsd";
//    TODO: Construct with appropriate content and document type for RDFs
//    private final static String contentType =
//            "application/rdf+xml";
//    private final static String docType = null;
    /**
     * Called by reflection
     * @param properties   not used
     */
    public XML2edm(Properties properties) {
        super(schemaLocation);
    }

    @Override
    public boolean isAvailableFor(Object nativeItem) {
        return true;
    }

    @Override
    public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException {
        if (nativeItem instanceof RecordInfo) {
            return ((RecordInfo) nativeItem).xml;
        }
        throw new CannotDisseminateFormatException(getSchemaLocation());
    }
}
