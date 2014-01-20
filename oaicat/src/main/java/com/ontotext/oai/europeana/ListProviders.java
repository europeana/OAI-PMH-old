package com.ontotext.oai.europeana;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Simo on 13-12-18.
 */
public class ListProviders extends ArrayList<Provider> {
    public boolean success;
    public int itemsCount;
    public long totalResults;
    String error;

    public ListProviders(String json) {
        if (json == null) {
            return;
        }
        JSONParser parser = new JSONParser();
        try {
            JSONObject jo = (JSONObject) parser.parse(json);
            success = (Boolean) jo.get("success");
            if (!success) {
                error = (String) jo.get("error");
                System.out.println(error);
                return;
            }
            itemsCount = ((Long)jo.get("itemsCount")).intValue();
            totalResults = (Long)jo.get("totalResults");
            ensureCapacity(itemsCount);
            for (Object item : ((JSONArray)jo.get("items"))) {
                add(getProvider((JSONObject) item));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Provider getProvider(JSONObject item) {
        String identifier = (String)item.get("identifier");
        String name = (String)item.get("name");
        return new Provider(identifier,  name);
    }

    private static final boolean LOAD_CACHE = true;
    private static final boolean SAVE_CACHE = false;
    private static final File cacheFile = new File("data", "listProviders.json");
    private static final File outFile = new File("data", "outSets.xml");
    private static final String testUrl =
            "http://europeana.eu/api/v2/providers.json?wskey=api2demo";

    public static void main(String[] args) throws IOException {
        String json;
        if (LOAD_CACHE) {
            json = FileUtils.readFileToString(cacheFile);
        } else {
            json = IOUtils.toString(new URL(testUrl), "UTF-8");
            if (SAVE_CACHE) {
                FileUtils.writeStringToFile(cacheFile,  json);
            }
        }


        ListProviders listProviders = new ListProviders(json);
        StringBuilder sbUrl = new StringBuilder(200);
        StringBuilder sbSets = new StringBuilder(20000);
        sbSets.append("<listSets>");
        for (Provider provider : listProviders ) {
            sbUrl.setLength(0);
            sbUrl.append("http://europeana.eu/api/v2/provider/");
            sbUrl.append(provider.identifier);
            sbUrl.append("/datasets.json?wskey=api2demo");
            String urlSets = sbUrl.toString();
            System.out.println(urlSets);
            String jsonSets = IOUtils.toString(new URL(urlSets), "UTF-8");
            ListSets sets = new ListSets(jsonSets);
            for (DataSet set : sets) {
                sbSets.append(set);
            }
        }
        sbSets.append("</listSets>");
        FileUtils.writeStringToFile(outFile,  sbSets.toString());
    }
}
