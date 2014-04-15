package com.ontotext;

import com.ontotext.iterator.SetsIteratorAdaptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import com.ontotext.process.ListProcessor;
import com.ontotext.process.RecordProcessor;
import com.ontotext.process.list.ListProcessorHub;
import com.ontotext.process.list.TraceListProcessor;
import com.ontotext.process.record.CountRecords;
import com.ontotext.process.record.EmptyRecordProcessor;
import com.ontotext.process.record.SearchString;
import com.ontotext.query.QueryListRecords;
import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.SetsList;
import com.ontotext.stats.SetStats;
import com.ontotext.walk.*;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Simo
 * Date: 13-10-30
 * Time: 13:51
 */
public class Main implements Runnable {
    private static final int numThreads = 1;
    private static final File outputDirectory = new File("data");
    private static final Log log = LogFactory.getLog(Main.class);

    private static int nullRecords = 0;
    private static int goodRecords = 0;
    private static Map<String, SetStats> setStats = new HashMap<String, SetStats>(1000);
    private final Properties properties;
    QueryListRecords query;
    OaiPmhServer server;
    ListProcessorHub listProcessor;
    int numPages;

    Main(Properties properties) {

        this.properties = properties;
        query = QueryListRecords.load(properties);
        String serverUrl = properties.getProperty("server", "http://localhost:8080/oaicat/OAIHandler");
        server = new OaiPmhServer(serverUrl);
        listProcessor = ListProcessorHub.load(properties);
        numPages = Integer.parseInt(properties.getProperty("numPages", "0"));
    }

    private static void printStats(Map<String, SetStats> m) {
        for (Map.Entry<String, SetStats> entry : m.entrySet()) {
            SetStats setStats = entry.getValue();
            System.out.println(entry.getKey() + " : " + setStats.good + "/" + setStats.size());
        }
    }

    private static boolean debugStop() {
        boolean stop = false;
        return stop;
    }

    private void iterateSets(Iterator<String> itSets, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            while (itSets.hasNext()) {
                String set = itSets.next();
                QueryListRecords query = new QueryListRecords(null, null, set);
                File outFile = new File(outputDirectory, set + ".out");
                try {
                    PrintStream out = new PrintStream(new FileOutputStream(outFile));
                    RecordProcessor recordProcessor = new CountRecords(properties);
                    ListProcessor listProcessor = new TraceListProcessor(properties);
                    Navigator<RecordsList> navigator = new PageCountNavigator(numPages);
                    ListRecordsWalker walker = new ListRecordsWalker(server, recordProcessor, listProcessor, query, navigator);
                    executor.execute(new WalkFinalizer(out, walker));
                } catch (FileNotFoundException e) {
                    log.error(e);
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    private void iterateSets(String host) {
        OaiPmhServer server = new OaiPmhServer(host);

        QueryListRecords query = new QueryListRecords(null, null, null);
//        QueryListRecords query = new QueryListRecords(null, null, "2022036");
//        QueryListRecords query = new QueryListRecords(null, null, "2023823");
//        QueryListRecords query = new QueryListRecords(null, null, "03486");

        RecordProcessor recordProcessor = new SearchString(properties);
//        RecordProcessor recordProcessor = new CollectionsStats();

        ListProcessor listProcessor = new TraceListProcessor(null);
//        ListProcessor listProcessor = new TimeMeasureProcessor();

        PageCountNavigator navigator = new PageCountNavigator(numPages);
        ListRecordsWalker walker = new ListRecordsWalker(server, recordProcessor, listProcessor, query, navigator);

        try {
            walker.run();
            recordProcessor.processRecordEnd();
            listProcessor.processListFinish();
        } catch (Exception e) {
            log.error(e); // is this possible?!
        }
    }

    private Iterator<String> getAllSets() {
        try {
            SetsList sets = server.listSets();
            return new SetsIteratorAdaptor(sets.asList().iterator());
        } catch (OAIException e) {
            log.error(e);
        }

        return null;
    }

    private static final String[] sampleSets = {
            "2022037",
            "2023836",
            "2022043",
            "2022026",
            "8536",
            "2022036",
            "2022042",
            "2022054",
            "2022062",
            "2022046",
            "2022052",
            "2022001",
            "2022063",
            "2022056",
            "2022031",
            "2022057",
            "2022051",
            "2022058",
            "2022059",
            "2022045",
            "2022060",
            "2022002",
            "2022061",
            "2023837",
            "2022030",
            "2022044",
            "2022053",
            "2022015",
            "2022014",
            "2022035",
            "2022064",
            "2022019",
            "2022028",
            "2022033",
            "2022034",
            "2022027",
            "2022032",
            "2022016",
            "2022029",
            "15411",
    };

    // good sets
    private static final String[] sets1 = {
            "11603",
            "11605",
            "2023810",
            "2023817",
            "11616",
            "11602",
            "2021608",
            "91617",
    };


    // small sets
    private static final String[] sets2 = {
            "2022029",
            "08544",
            "9200181",
            "9200180",
            "9200195",
            "2022014",
            "9200168",
    };

    // suspicious sets
    private static final String[] sets3 = {
            "3902",
            "3486",
            "2023601",
            "90402",
            "91626",
            "91640",
            "9200191",
            "91617",
            "92085",
            "9200103",
    };

    public void setSetStats(String host) throws Exception {
        OaiPmhServer server = new OaiPmhServer(host);
//        Identification result = server.identify();

        RecordsList recordsList = server.listRecords("edm", null, null, null);
        for  (int i = 0; i != numPages; ++i) {
            if (debugStop()) {
                break;
            }

            for (Record record : recordsList.asList()) {
                Element metadata = record.getMetadata();
                if (metadata == null) {
                    ++nullRecords;
                } else {
                    ++goodRecords;
                }
            }

            System.out.println(Integer.toString(i) + " Bad: " + nullRecords + " Good: " + goodRecords);

            if (recordsList.size() == 0) {
                break;
            }
            if (recordsList.getResumptionToken() != null) {
                recordsList = server.listRecords(recordsList.getResumptionToken());
            }
        }

        printStats(setStats);
    }

    private static Properties loadProperties(String[] args) {
        Properties properties = new Properties();
        final String fileName = (args.length == 0) ? "client.properties" : args[0];
        try {
            FileInputStream input = new FileInputStream(fileName);
            try {
                properties.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                input.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
        return properties;
    }

    public void run() {
        Navigator<RecordsList> navigator = (numPages == 0)
                ? new StandardNavigator()
                : new PageCountNavigator(numPages);

        ListRecordsWalker walker = new ListRecordsWalker(
                server, new EmptyRecordProcessor(), listProcessor, query, navigator);
        walker.run();
        listProcessor.processListFinish();
    }

    public static void main(String[] args) {
        Properties properties = loadProperties(args);
        Main main = new Main(properties);
        main.run();

//        try {
//            setSetStats(args);
//        } catch (Exception e) {
//            log.error(e);
//        }
//        OaiPmhServer server = new OaiPmhServer(host);
//        iterateSets();
//        iterateSets( Arrays.asList(sets3).com.ontotext.iterator(), numThreads);

//        main.iterateSets(main.getAllSets(), numThreads);

//        iterateSets(Arrays.asList(new String[] {null}).com.ontotext.iterator(), numThreads);
    }

}
