package com.ontotext.process.record;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.ontotext.helper.Oai4jUtil;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.RiotException;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.OutHolder;
import com.ontotext.process.RecordProcessor;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Created by Simo on 14-3-6.
 */
public class RdfValidator extends OutHolder implements RecordProcessor, ListProcessor {
    int numRecords = 0;
    int emptyRecords = 0;
    int numErrors = 0;

    public RdfValidator(Properties properties) {
        super(properties.getProperty("RdfValidator.logFile"), LogFactory.getLog(RdfValidator.class));
    }

    public void processRecord(Record record) {
        ++numRecords;
        String metadata = Oai4jUtil.getMetadata(record);
        if (metadata == null ) {
            ++emptyRecords;
            return;
        }
        Model m = ModelFactory.createDefaultModel();
        RDFReader rdfReader = m.getReader();
        try {
            rdfReader.read(m, new ByteArrayInputStream(metadata.getBytes()), null);
        }  catch (RiotException e) {
            ++numErrors;
            logError(Oai4jUtil.getId(record), e);

        } finally {
            m.close();
        }
    }

    private void trace(PrintStream out) {
        out.println("Num Records: " + numRecords);
        out.println("Empty records: " + emptyRecords);
        out.println("RDF Errors: " + numErrors);
    }
    public void processRecordEnd() {
        trace(out);
        super.close();
    }

    private void logError(String recordId, RiotException e) {
        out.println("RDF Error: " + " RecordId: " +  recordId + " Message: " + e.getMessage());
    }

    private void logSubTotal() {
        trace(System.out);
        System.out.println();
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
