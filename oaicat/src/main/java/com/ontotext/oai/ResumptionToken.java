package com.ontotext.oai;

import com.ontotext.oai.europeana.RegistryInfo;
import com.ontotext.oai.europeana.db.CloseableIterator;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Simo on 14-1-21.
 */
public class ResumptionToken implements Iterator<RegistryInfo>{
    private final CloseableIterator<RegistryInfo> dbCursor;
    private Date expirationDate;
    private final String id;
    private long cursor = 0L;
    private static final int EXPIRE_MINUTES = 10;
    private static final TokenGen tokenGen = new TokenGen();



    public ResumptionToken(CloseableIterator<RegistryInfo> dbCursor, long id) {
        this.dbCursor = dbCursor;
        this.id = tokenGen.createTokenId(id);
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
        RegistryInfo record = dbCursor.next();
        boolean deleted = record.deleted;
        // TODO: temp patch until 'deleted' flag became correct.
        if (!deleted) {
            ++cursor;
        }
        DateTime now = new DateTime(new Date());
        expirationDate = now.plusMinutes(EXPIRE_MINUTES).toDate();

        return  record;
    }

    @Override
    public void remove() {}

    public void close() {
        dbCursor.close();
    }

    private static class TokenGen {
        private static final int RANDOM_CHARS = 5;

        private Random random = new Random();
        public String createTokenId(long id) {
            StringBuilder sb = new StringBuilder(RANDOM_CHARS+20); // some extra space for number
            for (int i = 0; i != RANDOM_CHARS; ++i) {
                sb.append((char)((int)'A' + random.nextInt(26)));
            }
            sb.append('_');
            sb.append(id);
            return sb.toString();
        }
    }
}
