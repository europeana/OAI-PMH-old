package com.ontotext.oai.europeana;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

/**
 * Created by Simo on 13-12-18.
 */
public class ListSets extends ArrayList<DataSet> {
    public boolean success;
    public int itemsCount;
    public long totalResults;
    String error;
    private Log log = LogFactory.getLog(ListSets.class);

    public ListSets(String json) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jo = (JSONObject) parser.parse(json);
            success = (Boolean) jo.get("success");
            if (!success) {
                error = (String) jo.get("error");
                log.error(error);
                return;
            }
            itemsCount = ((Long)jo.get("itemsCount")).intValue();
            totalResults = (Long)jo.get("totalResults");
            if (itemsCount > 0) {
                ensureCapacity(itemsCount);
                for (Object item : ((JSONArray)jo.get("items"))) {
                    add(getDataSet((JSONObject) item));
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static DataSet getDataSet(JSONObject item) {
        String identifier = (String)item.get("identifier");
        String name = (String)item.get("name");
        return new DataSet(identifier,  name);
    }
}
