package com.ontotext.oai.europeana.db;

import com.mongodb.DBCursor;
import com.ontotext.oai.europeana.RegistryInfo;

import java.util.Iterator;

/**
 * Created by Simo on 14-1-21.
 */
public class RegistryRecordIterator implements Iterator<RegistryInfo> {
    private final DBCursor cursor;
    public RegistryRecordIterator(DBCursor cursor) {
        this.cursor = cursor;
    }

    public boolean hasNext() {
        boolean result;
        if (!(result = cursor.hasNext())) {
            cursor.close();
        }
        return result;
    }

    public RegistryInfo next() {
        RegistryRecord record = ((RegistryRecord)cursor.next());
        return new RegistryInfo(record.cid(),  record.eid(),  record.last_checked(),  record.orig(), record.deleted());
    }

    public void remove() {
        cursor.remove();
    }
}
