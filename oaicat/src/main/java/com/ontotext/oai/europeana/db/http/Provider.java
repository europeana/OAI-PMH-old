package com.ontotext.oai.europeana.db.http;

/**
 * Created by Simo on 13-12-18.
 */
public final class Provider {
    public final String identifier;
    public final String name;

    public Provider(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }
}
