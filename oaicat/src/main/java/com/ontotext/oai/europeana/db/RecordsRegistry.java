package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.RegistryInfo;

import java.util.Date;

/**
 * Created by Simo on 4.6.2014 г..
 */
public interface RecordsRegistry {
    RegistryInfo getRegistryInfo(String recordId);
    CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String setId);
    void close();
}
