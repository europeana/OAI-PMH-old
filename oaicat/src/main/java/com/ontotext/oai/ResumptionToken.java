package com.ontotext.oai;

import com.mongodb.DBCursor;
import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.RegistryRecord;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by Simo on 14-1-21.
 */
public class ResumptionToken implements Iterator<RegistryInfo>{
    private final DBCursor dbCursor;
    private Date expirationDate;
    private final String id;
    private long cursor = 0L;
    private final int EXPIRE_MINUTES = 10;


    public ResumptionToken(DBCursor dbCursor, long id) {
        this.dbCursor = dbCursor;
        this.id = "tokenId_" + id;
        DateTime now = new DateTime(new Date());
        expirationDate = now.plusMinutes(EXPIRE_MINUTES).toDate();
    }

    public String getId() {
        return id;
    }

    public long getCursor() {
        return cursor;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public boolean hasNext() {
        return dbCursor.hasNext();
    }

    @Override
    public RegistryInfo next() {
        RegistryRecord record = (RegistryRecord) dbCursor.next();
        String cid = record.cid();
        String eid = record.eid();
        Date date = record.last_checked();
        String orig = record.orig();
        ++cursor;
        DateTime now = new DateTime(new Date());
        expirationDate = now.plusMinutes(EXPIRE_MINUTES).toDate();

        return  new RegistryInfo(cid, eid, date, orig);
    }

    @Override
    public void remove() {}

    public void close() {
        dbCursor.close();
    }
}
