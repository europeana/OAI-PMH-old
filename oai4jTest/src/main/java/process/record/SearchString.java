package process.record;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import process.OutHolder;
import process.RecordProcessor;
import se.kb.oai.pmh.Record;
import se.kb.xml.XMLUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simo on 14-2-21.
 */
public class SearchString extends OutHolder implements RecordProcessor {
    private String s;
    int count = 0;

    public SearchString(Properties properties) {
        super("SearchString.logFile", LogFactory.getLog(SearchString.class));
        this.s = properties.getProperty("SearchString.s", "\"#");
    }
    public void processRecord(Record record) {
        try {
            Element metadataElement = record.getMetadata();
            if (metadataElement != null) {
                String metadata = XMLUtils.xmlToString(metadataElement);
                ++count;
                if (metadata.contains(s)) {
                    out.println(record.getHeader().getIdentifier());
                }
            }

//            XMLUtils.xmlToString(getMetadata());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRecordEnd() {
        out.println(count);
        super.close();
    }
}
