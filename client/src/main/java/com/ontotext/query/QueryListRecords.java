package com.ontotext.query;

import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public final class QueryListRecords {
    public final String prefix;
    public final String from;
    public final String until;
    public final String set;

    public QueryListRecords(String from, String until, String set) {
        this(from, until, set, "edm");

    }
    public QueryListRecords(String from, String until, String set, String prefix) {
        this.from = from;
        this.until = until;
        this.set = set;
        this.prefix = prefix;
    }

    public static QueryListRecords load(Properties properties) {
        String from = properties.getProperty("QueryListRecords.from");
        String until = properties.getProperty("QueryListRecords.until");
        String set = properties.getProperty("QueryListRecords.set");

        if (from == null && until == null && set == null) {
            return null;
        }

        String prefix = properties.getProperty("QueryListRecords.prefix", "edm");
        return new QueryListRecords(from, until, set, prefix);
    }

    public String toString() {
        return "Set: " + set + ", from: " + from + ", until: " + until;
    }

}
