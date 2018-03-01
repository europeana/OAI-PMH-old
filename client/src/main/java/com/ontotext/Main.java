package com.ontotext;

import com.ontotext.helper.Util;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.identifier.EmptyHeaderProcessor;
import com.ontotext.process.list.CountListProcessor;
import com.ontotext.process.list.EmptyListProcessor;
import com.ontotext.process.list.ListProcessorHub;
import com.ontotext.process.record.EmptyRecordProcessor;
import com.ontotext.query.BaseListQuery;
import com.ontotext.query.QueryListIdentifiers;
import com.ontotext.query.QueryListRecords;
import com.ontotext.walk.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.kb.oai.pmh.IdentifiersList;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.RecordsList;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.OperationsException;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-10-30
 * Time: 13:51
 */
public class Main implements Runnable {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private List<BaseListQuery> query = new ArrayList<>();
    private OaiPmhServer server;
    private ListProcessor<?> listProcessor;
    private int numPages;
    private boolean listIdentifiers;

    public Main(Properties properties) {
        this.listIdentifiers = Boolean.valueOf(properties.getProperty("verb", "listRecords").equals("listIdentifiers"));
        init(properties);
    }

    private void init(Properties properties) {
        String serverUrl = properties.getProperty("server", "http://localhost:8080/oaicat/OAIHandler");
        server = new OaiPmhServer(serverUrl);
        numPages = Integer.parseInt(properties.getProperty("numPages", "0"));

        if (listIdentifiers) {
            query.addAll(QueryListIdentifiers.loadMultiple(properties));
            listProcessor = new CountListProcessor();
        } else {
            BaseListQuery q = QueryListRecords.load(properties);
            if (q != null) {
                query.add(q);
            }
            listProcessor = ListProcessorHub.load(properties);
        }
    }


    private void registerMBean(Navigator navigator) throws OperationsException, MBeanException {
        OaiClientControl control = new OaiClientControl(navigator);
        ObjectName objectName = new ObjectName("com.ontotext:type=OaiClientControl");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(control,  objectName);
    }

    @Override
    public void run() {
        if (listIdentifiers) {
            runListIdentifiers();
        } else {
            runListRecords();
        }
    }

    private void runListIdentifiers() {
        Navigator<IdentifiersList> navigator = new StandardNavigator<>();
        try {
            registerMBean(navigator);
        } catch (OperationsException | MBeanException e) {
            LOG.error(e);
        }
        if (query != null) {
            runListIdentifiersQuery(navigator);
        }
    }

    private void runListIdentifiersQuery(Navigator<IdentifiersList> navigator) {
        for (BaseListQuery q : query) {
            ListIdentifiersWalker walker = new ListIdentifiersWalker(
                    server, new EmptyHeaderProcessor(), (ListProcessor<IdentifiersList>) listProcessor, (QueryListIdentifiers) q, navigator);
            LOG.info("Single list identifiers query: {}", q);
            long start = System.currentTimeMillis();
            try {
                walker.run();
            } finally {
                LOG.info("Query execution time: {}", String.valueOf(System.currentTimeMillis() - start));
                listProcessor.processListFinish();
            }
        }
    }

    private void runListRecords() {
        Navigator<RecordsList> navigator = (numPages == 0)
                ? new StandardNavigator<RecordsList>()
                : new PageCountNavigator(numPages);
        try {
            registerMBean(navigator);
        } catch (OperationsException | MBeanException e) {
            LOG.error(e);
        }

        if (query != null && !query.isEmpty()) {
            run1Query(navigator);
        }
        File setsFile = new File("sets.txt");
        if (setsFile.exists()) {
            LOG.info("Starting multiset queries.");
            try {
                List<String> sets = FileUtils.readLines(setsFile, "UTF-8");
                for (String set : sets) {
                    try {
                        LOG.info("Start set: {}", set);
                        QueryListRecords setQuery = new QueryListRecords(null, null, set);
                        ListRecordsWalker walker = new ListRecordsWalker(
                                server, new EmptyRecordProcessor(), listProcessor, setQuery, navigator);
                        walker.run();
                    } catch (Exception e) {
                        LOG.error("Set: {}", set, e);
                        throw new RuntimeException(e);
                    } finally {
                        listProcessor.processListFinish();
                    }
                    LOG.info("End set: {}", set);
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    public void run1Query(Navigator<RecordsList> navigator) {
        ListRecordsWalker walker = new ListRecordsWalker(
                server, new EmptyRecordProcessor(), listProcessor, (QueryListRecords) query.get(0), navigator);
        LOG.info("Single query: {}", query);
        try {
            walker.run();
        } finally {
            listProcessor.processListFinish();
        }
    }

    private static void setAuthentication(Properties properties) {
        final String username = properties.getProperty("username");
        final String password = properties.getProperty("password");

        if (username != null && password != null)
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
    }

    public static void main(String[] args) {
        try {
            Properties properties = Util.loadProperties();
            setAuthentication(properties);
            Main main = new Main(properties);
            LOG.info("Begin: {}", new Date());
            main.run();
            LOG.info("End: {}", new Date());
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
