package com.ontotext.process;

import com.ontotext.helper.Oai4jUtil;
import org.apache.commons.logging.LogFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simo on 14-3-12.
 */
public class OwlimUpdater extends OutHolder implements RecordProcessor, ListProcessor {
    RepositoryConnection repository;
    public OwlimUpdater(Properties properties) {
        super(properties.getProperty("OwlimUpdater.logFile"), LogFactory.getLog(OwlimUpdater.class));
        String server = properties.getProperty("OwlimUpdater.server", "http://localhost:8080/openrdf-sesame");
        String repositoryID = properties.getProperty("OwlimUpdater.repositoryID" ,"europeana");
        repository = getConnection(server,  repositoryID);

    }

    private static RepositoryConnection getConnection(String sesameServer, String repositoryID) {
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        RepositoryConnection connecton = null;

        try {
            repo.initialize();
            connecton = repo.getConnection();

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return connecton;
    }


    public void processListBegin(RecordsList recordsList) {
        if (repository == null) {
            return;
        }

        try {
            repository.begin();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void processListEnd(RecordsList recordsList) {
        if (repository == null) {
            return;
        }

        try {
            repository.commit();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void processListFinish() {
        if (repository == null) {
            return;
        }

        try {
            repository.close();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void processListError(Exception e) {
        processListFinish();
    }

    public void processRecord(Record record) {
        if (repository == null) {
            return;
        }

        String metadata = Oai4jUtil.getMetadata(record);
        if (metadata == null ) {
            return;
        }

        try {
            repository.add(new ByteArrayInputStream(metadata.getBytes()), "", RDFFormat.RDFXML);
        } catch (Exception e) {
            out.println("Record: " + record.getHeader().getIdentifier() + " Error: " + e.getMessage());
        }
    }

    public void processRecordEnd() {

    }
}
