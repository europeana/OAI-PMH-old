package com.ontotext.query;

public class BaseListQuery {
    private final String prefix;
    private final String from;
    private final String until;
    private final String set;

    protected BaseListQuery(String from, String until, String set) {
        this(from, until, set, "edm");

    }

    protected BaseListQuery(String from, String until, String set, String prefix) {
        this.from = from;
        this.until = until;
        this.set = set;
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getFrom() {
        return from;
    }

    public String getUntil() {
        return until;
    }

    public String getSet() {
        return set;
    }

    public String toString() {
        return "Set: " + set + ", from: " + from + ", until: " + until;
    }
}
