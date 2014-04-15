package com.ontotext.helper;

import org.dom4j.Element;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;
import se.kb.xml.XMLUtils;

import java.io.IOException;

/**
 * Created by Simo on 14-3-7.
 */
public class Oai4jUtil {
    public static String getMetadata(Record record) {
        Element metadataElement = record.getMetadata();

        String metadata = null;
        if (metadataElement != null) {
            try {
                metadata = XMLUtils.xmlToString(metadataElement);
            } catch (IOException e) {
                // shouldn't be possible to reach here.
                e.printStackTrace();
            }
        }

        return metadata;
    }

    public static String getId(Record record) {
        String id = null;
        Header header = record.getHeader();
        if (header != null) {
            id = header.getIdentifier();
        }

        return id;
    }
}
