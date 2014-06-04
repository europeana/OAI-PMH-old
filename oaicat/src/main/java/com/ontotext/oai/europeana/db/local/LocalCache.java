package com.ontotext.oai.europeana.db;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Created by Simo on 14-1-15.
 */
public class LocalCache {
    private File baseDir;
    private File providersDir;

    private static final String FN_PROVIDERS = "providers.json";
    private static final String FN_DATA_SETS = "datasets.json";

    public LocalCache(File baseDir) {
        this.baseDir = baseDir;
        providersDir = new File(baseDir, "providers");
    }

    public void saveProviders(String json) {
        Util.saveFileNoThrow(getProvidersFile(), json);
    }

    public String loadProviders() {
        return Util.loadFileNoThrow(getProvidersFile());
    }

    public void saveProviderSets(String providerId, String jsonSets) {
        Util.saveFileNoThrow(getProviderSetsFile(providerId), jsonSets);
    }


    public String loadProviderSets(String providerId) {
        return Util.loadFileNoThrow(getProviderSetsFile(providerId));
    }

    public void saveRecord(String recordId, String rdf) {
        Util.saveFileNoThrow(getRecordFile(recordId), rdf);
    }

    String loadRecord(String recordId) {
        return Util.loadFileNoThrow(getRecordFile(recordId));
    }

    private File getProvidersFile() {
        return new File(baseDir,  FN_PROVIDERS);
    }

    private File getProviderSetsFile(String providerId) {
        providerId = FilenameUtils.separatorsToSystem(providerId);
        return new File(getProviderDir(providerId), FN_DATA_SETS);
    }

    private File getProviderDir(String providerId) {
        providerId = FilenameUtils.separatorsToSystem(providerId);
        return new File(providersDir, providerId);
    }

    private File getRecordFile(String recordId) {
        String provider = null;
        String record = null;

        StringTokenizer tokenizer = new StringTokenizer(recordId, "/");
        if (tokenizer.hasMoreElements()) {
            provider = tokenizer.nextToken();
            if (tokenizer.hasMoreElements()) {
                record = tokenizer.nextToken();
            }
        }

        provider = FilenameUtils.separatorsToSystem(provider);
        record = FilenameUtils.separatorsToSystem(record);

        File recordFile;
        if (provider != null && record != null) {
            recordFile = new File(getProviderDir(provider), record + ".rdf");
        } else {
            recordId = FilenameUtils.separatorsToSystem(recordId);
            recordFile = new File(baseDir, recordId + ".rdf");
        }

        return recordFile;
    }

    public static LocalCache getDummyCache() {
        return new LocalCache(new File(".")) {
            @Override
            public void saveProviders(String json) {}

            @Override
            public String loadProviders() { return null; }

            @Override
            public void saveProviderSets(String providerId, String jsonSets) {}

            @Override
            public String loadProviderSets(String providerId) { return null; }

            @Override
            public void saveRecord(String recordId, String rdf) {}

            @Override
            String loadRecord(String recordId) { return null; }
        };
    }
}
