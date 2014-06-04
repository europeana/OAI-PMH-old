package com.ontotext.oai.europeana.db.mongodb;

import com.mongodb.DBCursor;
import com.ontotext.oai.europeana.db.CloseableIterator;

/**
 * Created by Simo on 4.6.2014 г..
 * Thin wrapper for common interface
 */
public class MongoDbCurosr<T> implements CloseableIterator<T>{
    private final DBCursor dbCursor;

    public MongoDbCurosr(DBCursor dbCursor) {
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
    public T next() {
        return (T) dbCursor.next();
    }

    @Override
    public void remove() {
        dbCursor.remove();
    }
}
