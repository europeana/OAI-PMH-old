package com.ontotext.process.record;

import com.ontotext.process.RecordProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import se.kb.oai.pmh.Record;
import se.kb.xml.XMLUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simo on 14-2-21.
 */
public class SearchString implements RecordProcessor {
    private static final Log log = LogFactory.getLog(SearchString.class);
    private String s;
    int count = 0;

    public SearchString(Properties properties) {
        this.s = properties.getProperty("SearchString.s", "\"#");
    }
    public void processRecord(Record record) {
        try {
            Element metadataElement = record.getMetadata();
            if (metadataElement != null) {
                String metadata = XMLUtils.xmlToString(metadataElement);
                ++count;
                if (metadata.contains(s)) {
                    log.info(record.getHeader().getIdentifier());
                }
            }

//            XMLUtils.xmlToString(getMetadata());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRecordEnd() {
        log.info(count);
    }
}
