package com.ontotext.process.record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.ontotext.helper.ByteArrayOutputStream2;
import com.ontotext.helper.Oai4jUtil;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.RiotException;
import org.dom4j.Element;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.xml.XMLUtils;

import java.util.Properties;

/**
 * Created by Simo on 14-3-6.
 */
public class RdfValidator implements RecordProcessor, ListProcessor {
    private static final Log log = LogFactory.getLog(RdfValidator.class);
    private static final Log rdfLog = LogFactory.getLog("RDF");
    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    int numRecords = 0;
    int emptyRecords = 0;
    int numErrors = 0;

    public RdfValidator(Properties properties) {
    }

    public void processRecord(Record record) {
        ++numRecords;

        ByteArrayOutputStream2 metadataStream = new ByteArrayOutputStream2(BUFFER_SIZE);
        Model m = ModelFactory.createDefaultModel();
        RDFReader rdfReader = m.getReader();
        try {
            Element metadata = record.getMetadata();
            boolean noMetadata = false;
            if (metadata == null) {
                noMetadata = true;
                ++emptyRecords;
            } else {
                XMLUtils.writeXmlTo(metadata, metadataStream);
                if (metadataStream.size() != 0) {
                    rdfReader.read(m, metadataStream.toInputStream(), null);
                } else {
                    noMetadata = true;
                    ++emptyRecords;
                }
            }
            if (noMetadata) {
                log.warn("No metadata, Record: " + Oai4jUtil.getId(record));
            }
        }  catch (RiotException e) {
            ++numErrors;
            logRDFError(Oai4jUtil.getId(record), e);
        } catch (java.io.IOException e) {
            log.error(e);
        } finally {
            m.close();
        }
    }

    private void trace() {
        log.info("Num Records: " + numRecords);
        log.info("Empty records: " + emptyRecords);
        log.info("RDF Errors: " + numErrors);
    }
    public void processRecordEnd() {
        trace();
    }

    private void logRDFError(String recordId, RiotException e) {
        rdfLog.error("RecordId: " +  recordId + " Message: " + e.getMessage());
    }

    private void logSubTotal() {
        trace();
    }

    public void processListBegin(RecordsList recordsList) {

    }

    public void processListEnd(RecordsList recordsList) {
        logSubTotal();
    }

    public void processListFinish() {

    }

    public void processListError(Exception e) {

    }
}
