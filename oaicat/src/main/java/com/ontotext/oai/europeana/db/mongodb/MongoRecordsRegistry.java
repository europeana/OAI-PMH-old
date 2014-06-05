package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.BasicDBObject;

import java.util.Date;

import static com.ontotext.oai.europeana.db.mongodb.RegistryFields.*;

/**
 * Created by Simo on 14-1-10.
 */
public class MongoRecordsRegistry extends BasicDBObject {
    public String eid() {
        return super.getString(KEY_RECORD_ID);
    }

    public String cid() {
        return super.getString(KEY_COLLECTION);
    }

    public Date last_checked() {
        return super.getDate(KEY_DATE);
    }

    public boolean deleted() {
        return super.getBoolean(KEY_DELETED, false);
    }
}
