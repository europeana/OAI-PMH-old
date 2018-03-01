package com.ontotext.process.record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.ontotext.helper.ByteArrayOutputStream2;
import com.ontotext.helper.Oai4jUtil;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import org.apache.jena.riot.RiotException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Element;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.xml.XMLUtils;

/**
 * Created by Simo on 14-3-6.
 */
public class RdfValidator implements RecordProcessor, ListProcessor<RecordsList> {

    private static final Logger LOG = LogManager.getLogger(RdfValidator.class);

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    int numRecords = 0;
    int emptyRecords = 0;
    int numErrors = 0;


    public void processRecord(Record record) {
        ++numRecords;

        Model m = null;
        try (ByteArrayOutputStream2 metadataStream = new ByteArrayOutputStream2(BUFFER_SIZE)){
            m = ModelFactory.createDefaultModel();
            RDFReader rdfReader = m.getReader();
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
                LOG.warn("No metadata, Record: {} ", Oai4jUtil.getId(record));
            }
        }  catch (RiotException e) {
            ++numErrors;
            logRDFError(Oai4jUtil.getId(record), e);
        } catch (java.io.IOException e) {
            LOG.error(e);
        } finally {
            m.close();
        }
    }

    private void trace() {
        LOG.info("Num Records: {}", numRecords);
        LOG.info("Empty records: {}", emptyRecords);
        LOG.info("RDF Errors: {}", numErrors);
    }
    public void processRecordEnd() {
        trace();
    }

    private void logRDFError(String recordId, RiotException e) {
        LOG.error("RecordId: {} Message: {}", recordId, e.getMessage());
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
