package query;

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
        return new QueryListRecords(
                properties.getProperty("QueryListRecords.from"),
                properties.getProperty("QueryListRecords.until"),
                properties.getProperty("QueryListRecords.set"),
                properties.getProperty("QueryListRecords.prefix", "edm")
                );
    }
}
