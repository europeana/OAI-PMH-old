package com.ontotext.process;

import com.ontotext.helper.ByteArrayOutputStream2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.ontotext.helper.Oai4jUtil.getId;

/**
 * Created by Simo on 14-3-12.
 */
public class OwlimUpdater implements RecordProcessor, ListProcessor {
    private static Log log = LogFactory.getLog(OwlimUpdater.class);
    RepositoryConnection repository;
    private static final int BUFFER_SIZE = 10*1024*1024; // 10 MB
    String server;
    String repositoryID;
    int numRecords;
    int badRecords;
    public OwlimUpdater(Properties properties) {
        server = properties.getProperty("OwlimUpdater.server", "http://localhost:8080/openrdf-sesame");
        repositoryID = properties.getProperty("OwlimUpdater.repositoryID" ,"europeana");
        repository = getConnection(server,  repositoryID);
    }

    private RepositoryConnection getConnection(String sesameServer, String repositoryID) {
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        RepositoryConnection connection = null;

        try {
            log.info("repo init() start");
            repo.initialize();
            connection = repo.getConnection();
            log.info("repo init() end");
        } catch (RepositoryException e) {
            log.error("repo init()", e);
        }

        return connection;
    }


    public void processListBegin(RecordsList recordsList) {
        if (repository == null) {
            log.error("repo is null");
            return;
        }

        try {
            if (repository.isActive()) {
                log.error("Transaction is active!");
                return;
            }
            log.info("repo begin() start");
            repository.begin();
            log.info("repo begin() end");
        } catch (RepositoryException e) {
            log.error("begin()", e);
        }
    }

    public void processListEnd(RecordsList recordsList) {
        if (repository == null) {
            log.error("repo is null");
            return;
        }

        try {
            log.info("OWLIM Commit start");
            repository.commit();
            log.info("OWLIM Commit end");
        } catch (RepositoryException e) {
            log.error("Exception on repo commit", e);
        }
    }

    public void processListFinish() {
        if (repository == null) {
            return;
        }

        printStats();
        clearStats();

        try {
            log.info("repo close()");
            repository.close();
            repository = getConnection(server,  repositoryID);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void processListError(Exception e) {
        processListFinish();
    }

    public void processRecord(Record record) {
        if (repository == null) {
            log.error("repo is null");
            return;
        }

        ++numRecords;

        ByteArrayOutputStream2 metadataStream = new ByteArrayOutputStream2(BUFFER_SIZE);

        try {
            Element metadata = record.getMetadata();
            if (metadata == null) {
                log.warn("No metadata, Record: " + getId(record));
            } else {
                XMLUtils.writeXmlTo(metadata, metadataStream);
                repository.add(metadataStream.toInputStream(), "", RDFFormat.RDFXML);
            }
        } catch (RepositoryException e) {
            log.error("repo add failed: " + getId(record), e);
            retryAdd(metadataStream.toInputStream());
        } catch (RDFParseException e) {
            ++badRecords;
            log.warn("Record: " + getId(record) + " Error: " + e.getMessage());
        } catch (IOException e) {
            log.error("processRecord() IO", e);
        }
    }

    /**
     * When an add failed, all adds till next commit fail too. So force commit and retry once.
     */
    private void retryAdd(ByteArrayInputStream metadataStream) {
        log.info("Retry add");
        try {
            repository.commit();
            repository.begin();
            repository.add(metadataStream, "", RDFFormat.RDFXML);
        } catch (Exception e) {
            log.error("Retry add", e);
        }
    }

    public void processRecordEnd() {
        log.info("processRecordEnd");
    }

    private void clearStats() {
        numRecords = 0;
        badRecords = 0;
    }

    private void printStats() {
        log.info("Num records:" + numRecords );
        log.info("Bad records:" + badRecords);
    }
}
