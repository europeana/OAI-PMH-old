package com.ontotext.oai.europeana.db;

import com.ontotext.oai.europeana.RegistryInfo;

import java.util.Date;

/**
 * Created by Simo on 4.6.2014 г..
 */
public interface RecordsRegistry {

    public RegistryInfo getRegistryInfo(String recordId);

    public CloseableIterator<RegistryInfo> listRecords(Date from, Date until, String setId);

    public void close();
}
