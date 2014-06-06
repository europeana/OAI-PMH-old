package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.DBCursor;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;

/**
 * Created by Simo on 6.6.2014 г..
 * Adaptor class MongoRecordsRegistry -> RegistryInfo
 */
public class MongoRegistryIterator  implements CloseableIterator<RegistryInfo>{
    DBCursor dbCursor;
    public MongoRegistryIterator(DBCursor dbCursor) {
        this.dbCursor = dbCursor;
    }

    @Override
    public void close() {
        dbCursor.close();
    }

    @Override
    public boolean hasNext() {
        return dbCursor.hasNext();
    }

    @Override
    public RegistryInfo next() {
        MongoRecordsRegistry record = (MongoRecordsRegistry)dbCursor.next();
        return new RegistryInfo(record.cid(),  record.eid(), record.last_checked(),  record.deleted());
    }

    @Override
    public void remove() {
        dbCursor.remove();
    }
}
