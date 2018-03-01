package com.ontotext.process;

import com.ontotext.helper.ByteArrayOutputStream2;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
public class OwlimUpdater implements RecordProcessor, ListProcessor<RecordsList> {

    private static Logger LOG = LogManager.getLogger(OwlimUpdater.class);

    private RepositoryConnection repository;
    private static final int BUFFER_SIZE = 10*1024*1024; // 10 MB
    private String server;
    private String repositoryID;
    private int numRecords;
    private int badRecords;

    public OwlimUpdater(Properties properties) {
        server = properties.getProperty("OwlimUpdater.server", "http://localhost:8080/openrdf-sesame");
        repositoryID = properties.getProperty("OwlimUpdater.repositoryID" ,"europeana");
        repository = getConnection(server,  repositoryID);
    }

    private RepositoryConnection getConnection(String sesameServer, String repositoryID) {
        Repository repo = new HTTPRepository(sesameServer, repositoryID);
        RepositoryConnection connection = null;

        try {
            LOG.debug("repo init() start");
            repo.initialize();
            connection = repo.getConnection();
            LOG.debug("repo init() end");
        } catch (RepositoryException e) {
            LOG.error("repo init()", e);
        }

        return connection;
    }


    public void processListBegin(RecordsList recordsList) {
        if (repository == null) {
            LOG.error("repo is null");
            return;
        }

        try {
            if (repository.isActive()) {
                LOG.error("Transaction is active!");
                return;
            }
            LOG.debug("repo begin() start");
            repository.begin();
            LOG.debug("repo begin() end");
        } catch (RepositoryException e) {
            LOG.error("begin()", e);
        }
    }

    public void processListEnd(RecordsList recordsList) {
        if (repository == null) {
            LOG.error("repo is null");
            return;
        }

        try {
            LOG.debug("OWLIM Commit start");
            repository.commit();
            LOG.debug("OWLIM Commit end");
        } catch (RepositoryException e) {
            LOG.error("Exception on repo commit", e);
        }
    }

    public void processListFinish() {
        if (repository == null) {
            return;
        }

        printStats();
        clearStats();

        try {
            LOG.debug("repo close()");
            repository.close();
            repository = getConnection(server,  repositoryID);
        } catch (RepositoryException e) {
            LOG.error(e);
        }
    }

    public void processListError(Exception e) {
        processListFinish();
    }

    public void processRecord(Record record) {
        if (repository == null) {
            LOG.error("repo is null");
            return;
        }

        ++numRecords;

        try (ByteArrayOutputStream2 metadataStream = new ByteArrayOutputStream2(BUFFER_SIZE)) {
            try {
                Element metadata = record.getMetadata();
                if (metadata == null) {
                    LOG.warn("No metadata, Record: {}", getId(record));
                } else {
                    XMLUtils.writeXmlTo(metadata, metadataStream);
                    repository.add(metadataStream.toInputStream(), "", RDFFormat.RDFXML);
                }
            } catch (RepositoryException e) {
                LOG.error("repo add failed: {}", getId(record), e);
                retryAdd(metadataStream.toInputStream());
            }
        }
        catch (IOException e) {
            LOG.error("processRecord() IO", e);
        }
        catch (RDFParseException e) {
            ++badRecords;
            LOG.warn("Error retrieving record {}", e);
        }
    }

    /**
     * When an add failed, all adds till next commit fail too. So force commit and retry once.
     */
    private void retryAdd(ByteArrayInputStream metadataStream) {
        LOG.info("Retry add");
        try {
            repository.commit();
            repository.begin();
            repository.add(metadataStream, "", RDFFormat.RDFXML);
        } catch (Exception e) {
            LOG.error("Retry add", e);
        }
    }

    public void processRecordEnd() {
        LOG.info("processRecordEnd");
    }

    private void clearStats() {
        numRecords = 0;
        badRecords = 0;
    }

    private void printStats() {
        LOG.info("Num records: {}", numRecords );
        LOG.info("Bad records: {}", badRecords);
    }
}
