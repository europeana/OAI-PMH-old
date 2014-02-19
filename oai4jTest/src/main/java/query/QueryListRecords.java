package query;

/**
 * Created by Simo on 14-1-30.
 */
public final class QueryListRecords {
    public final String metadataPrefix = "edm";
    public final String from;
    public final String until;
    public final String set;

    public QueryListRecords(String from, String until, String set) {
        this.from = from;
        this.until = until;
        this.set = set;
    }
}
