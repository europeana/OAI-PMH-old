package com.ontotext.oai;

import com.ontotext.oai.europeana.RegistryInfo;

import java.util.Date;

/**
 * Created by Simo on 14-1-9.
 */
public class RecordInfo {
    public final String xml;
    public final RegistryInfo registryInfo;

    public RecordInfo(String xml, RegistryInfo registryInfo) {
        this.xml = xml;
        this.registryInfo = registryInfo;
    }

    public String getLocalId() {
        if (registryInfo == null) {
            return null;
        }
        return registryInfo.eid;
    }

    public Date getTimeStamp() {
        if (registryInfo == null) {
            return null;
        }
        return registryInfo.last_checked;
    }

    public String getSetId() {
        if (registryInfo == null) {
            return null;
        }
        return registryInfo.cid;
    }

    public boolean isDeleted() {
        if (registryInfo == null) {
            return true;
        }
        return registryInfo.deleted;
    }

    public String getOriginalId () {
        if (registryInfo == null) {
            return null;
        }

        return registryInfo .orig;
    }

}
