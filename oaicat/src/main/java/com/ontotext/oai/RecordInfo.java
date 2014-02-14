package com.ontotext.oai;

import com.ontotext.oai.europeana.RegistryInfo;

/**
 * Created by Simo on 14-1-9.
 */
public class RecordInfo extends RegistryInfo {
    public final String xml;

    public RecordInfo(String xml, RegistryInfo registryInfo) {
        super(registryInfo);
        this.xml = xml;
    }
}
