package com.ontotext.oai.europeana;

/**
 * Created by Simo on 13-12-18.
 */
public final class DataSet {
    public final String identifier;
//    public final long provIdentifier;
    public final String name;
//    public final String status;
//    public final long publishedRecords;
//    public final long deletedRecords;
//    public final Date creationDate;
//    public final Date publicationDate;


    public DataSet(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }
}
