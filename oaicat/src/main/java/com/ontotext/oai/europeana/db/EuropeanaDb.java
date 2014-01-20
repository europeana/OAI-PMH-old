package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.DataSet;
import com.ontotext.oai.europeana.ListProviders;
import com.ontotext.oai.europeana.ListSets;
import com.ontotext.oai.europeana.Provider;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 13-12-12.
 */
public class EuropeanaDb {
    private String apiKey;
    private String baseUrl;

    private LocalCache cache;
    private boolean loadCache;
    private boolean saveCache;

    public EuropeanaDb(Properties properties) {
        apiKey = properties.getProperty("EuropeanaDb.apiKey", "api2demo");
        baseUrl = properties.getProperty("EuropeanaDb.baseUrl", "http://europeana.eu/api/v2/");
        String cacheDir = properties.getProperty("LocalCache.baseDir");
        if (cacheDir != null) {
            cache = new LocalCache(new File(cacheDir));
            loadCache = Boolean.parseBoolean(properties.getProperty("LocalCache.loadCache", "true"));
            saveCache = Boolean.parseBoolean(properties.getProperty("LocalCache.saveCache", "true"));
        } else {
            cache = LocalCache.getDummyCache();
            loadCache = false;
            saveCache = false;
        }
    }


    // id: /11601/database_detail_php_ID_187548 ->
    //http://europeana.eu/api/v2/record/11601/database_detail_php_ID_187548.rdf?
    // wskey=api2demo&profile=full
    public String getRecord(String id) {
        String rdf = null;
        if (loadCache) {
            rdf = cache.loadRecord(id);
        }

        if (rdf == null) {
            try {
                rdf = downloadRecord(id);
                if (saveCache) {
                    cache.saveRecord(id, rdf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rdf;
    }

    public List<DataSet> listSets() {
        List<DataSet> sets = new ArrayList<DataSet>();
        try {
            String jsonProviders = getProvidersJson();
            for (Provider provider : new ListProviders(jsonProviders)) {
                String jsonSets = getProviderDataSets(provider.identifier);
                sets.addAll(new ListSets(jsonSets));
                if (saveCache) {
                    cache.saveProviderSets(provider.identifier, jsonSets);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sets;
    }

    private String getProvidersJson() throws IOException {
        String json = null;
        if (loadCache) {
            json = cache.loadProviders();
        }

        if (json == null) {
            json = downloadProviders();
            if (saveCache) {
                if (json != null) {
                    cache.saveProviders(json);
                }
            }
        }

        return json;
    }

    private String getProviderDataSets(String providerId) throws IOException {
        String json = null;
        if (loadCache) {
            json = cache.loadProviderSets(providerId);
        }

        if (json == null) {
            json = downloadProviderDataSets(providerId);
            if (saveCache) {
                if (json != null) {
                    cache.saveProviderSets(providerId, json);
                }
            }
        }

        return json;
    }

    private String downloadProviderDataSets(String providerId) throws IOException {
        System.out.println("Download provider data sets: " + providerId);
        String urlSets = baseUrl + "provider/" + providerId + "/datasets.json" + "?wskey=" + apiKey;
        return IOUtils.toString(new URL(urlSets), "UTF-8");
    }

    private String downloadProviders() throws IOException {
        String urlProviders = baseUrl + "providers.json" + "?wskey=" + apiKey;
        return IOUtils.toString(new URL(urlProviders), "UTF-8");
    }

    private String downloadRecord(String id) throws IOException {
        String url = baseUrl + "record" + id + ".rdf" +"?wskey=" + apiKey;
        return IOUtils.toString(new URL(url));
    }


//    public String getSetId(String recordId) {
//        return parseCid(recordId);
//    }

    public void close() {
        // nothing to do
    }

    public static void main(String[] args) {
        Properties properties = new Properties();

        EuropeanaDb db = new EuropeanaDb(properties);
        List<DataSet> sets = db.listSets();
        System.out.println(sets);
    }
}
