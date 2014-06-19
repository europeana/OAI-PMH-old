package com.ontotext.process;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simo on 14-3-12.
 */
public class OwlimUpdater extends OutHolder implements RecordProcessor, ListProcessor {
    RepositoryConnection repository;
    private final int BUFFER_SIZE = 10*1024*1024; // 10 MB
    public OwlimUpdater(Properties properties) {
        super(properties.getProperty("OwlimUpdater.logFile"), LogFactory.getLog(OwlimUpdater.class));
        String server = properties.getProperty("OwlimUpdater.server", "http://localhost:8080/openrdf-sesame");
        String repositoryID = properties.getProperty("OwlimUpdater.repositoryID" ,"europeana");
        repository = getConnection(server,  repositoryID);

    }

    private static RepositoryConnection getConnection(String sesameServer, String repositoryID) {
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        RepositoryConnection connection = null;

        try {
            repo.initialize();
            connection = repo.getConnection();

        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return connection;
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

        ByteArrayOutputStream metadataStream = new ByteArrayOutputStream(BUFFER_SIZE);

        try {
            Element metadata = record.getMetadata();
            if (metadata == null) {
                System.out.println("Error: " + "No metadata, Record: " + getRecordId(record));
            } else {
                XMLUtils.writeXmlTo(record.getMetadata(), metadataStream);
                repository.add(new ByteArrayInputStream(metadataStream.toByteArray()), "", RDFFormat.RDFXML);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (RDFParseException e) {
            out.println("Record: " + getRecordId(record) + " Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRecordEnd() {

    }

    private static String getRecordId(Record record) {
        String id = null;
        if (record != null) {
            Header header = record.getHeader();
            if (header != null) {
                id = header.getIdentifier();
            }
        }

        return id;
    }

}
