package com.ontotext.oai.europeana;

import java.util.Date;

/**
 * Created by Simo on 14-1-9.
 */
public class RegistryInfo {
    public final String cid;
    public final String eid;
    public final Date last_checked;
    public final boolean deleted;

    public RegistryInfo(String cid, String eid, Date last_checked, boolean deleted) {
        this.eid = eid;
        this.cid = cid;
        this.last_checked = last_checked;
        this.deleted = deleted;
    }

    public RegistryInfo(RegistryInfo rhs) {
        this.eid = rhs.eid;
        this.cid = rhs.cid;
        this.last_checked = rhs.last_checked;
        this.deleted = rhs.deleted;
    }
}
