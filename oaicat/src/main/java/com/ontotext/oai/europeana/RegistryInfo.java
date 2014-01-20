package com.ontotext.oai.europeana;

import java.util.Date;

/**
 * Created by Simo on 14-1-9.
 */
public final class RegistryInfo {
    public final String cid;
    public final String eid;
    public final Date last_checked;
    public final String orig;
    public final boolean deleted = false; // not implemented

    public RegistryInfo(String cid, String eid, Date last_checked, String orig) {
        this.eid = eid;
        this.cid = cid;
        this.last_checked = last_checked;
        this.orig = orig;
    }

//    public final String getLocalId() {
//        return eid;
//    }
//
//    public final String getDataSetId() {
//        return cid;
//    }
//
//    public final Date getTimeStamp() {
//        return last_checked;
//    }
//
//    public final boolean isDeleted() {
//        return deleted;
//    }
}
