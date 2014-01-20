package com.ontotext.oai.europeana.db;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import java.util.Date;

import static com.ontotext.oai.europeana.db.RegistryFields.KEY_DATE;
import static com.ontotext.oai.europeana.db.RegistryFields.KEY_RECORD_ID;

/**
 * Created by Simo on 13-12-12.
 */

/**
 * Simo: All methods can be static if field names are static but prefer to let it as it is.
 * Queries can be cached.
 */
public class MongoUtil {


    public DBObject queryDateRange(Date begin, Date end) {
        DBObject query = new BasicDBObject();
        query.put(KEY_DATE,  BasicDBObjectBuilder.start("$gte", begin).add("$lt", end).get());
        return query;
    }

    public DBObject queryRecord(String recordId) {
        DBObject query = new BasicDBObject();
        query.put(KEY_RECORD_ID,  recordId);
        return query;
    }

//    public DBObject queryListSets() {
//        DBObject query = BasicDBObjectBuilder.start("distinct", "EuropeanaIdRegistry").add("key", KEY_COLLECTION).get();
//        System.out.println(query);
//        return query;
//    }
}
