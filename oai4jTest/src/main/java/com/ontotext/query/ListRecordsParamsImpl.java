package query;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by Simo on 14-2-20.
 */
public class ListRecordsParamsImpl implements ListRecordsParams {
    private final DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    private DateTime from;
    private DateTime until;
    private final String set;
    private final String prefix = "edm";

    public ListRecordsParamsImpl(Date from, Date until, String set) {
        this.from = new DateTime(from);
        this.until = new DateTime(until);
        this.set = set;
    }

    public ListRecordsParamsImpl(String from, String until, String set) {
        this.from = parse(from);
        this.until = parse(until);
        this.set = set;
    }
    public ListRecordsParamsImpl(QueryListRecords queryListRecords) {
        this(queryListRecords.from,  queryListRecords.until,  queryListRecords.set);
    }

    public String from() {
        return format(from);
    }

    public String until() {
        return format(until);
    }

    public String set() {
        return set;
    }

    public String prefix() {
        return prefix;
    }

    private String format(DateTime dateTime) {
        return fmt.print(dateTime);
    }

    private DateTime parse(String dateTime) {
        return new DateTime(dateTime);
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getUntil() {
        return until;
    }

    public ListRecordsParamsImpl setFrom(String from) {
        return setFrom(parse(from));
    }

    public ListRecordsParamsImpl setFrom(DateTime from) {
        this.from = from;
        if (this.until == null) {
            this.until = parse("9999-12-31T23:59:59Z");
        }

        return this;
    }

    public ListRecordsParamsImpl setUntil(DateTime until) {
        this.until = until;
        if (this.from == null) {
            from = parse("0001-01-01T00:00:00Z");
        }

        return this;
    }

    public ListRecordsParamsImpl setRange(DateTime from, DateTime until) {
        this.from = from;
        this.until = until;
        return this;
    }

    public QueryListRecords asQuery() {
        return new QueryListRecords(format(from), format(until), set);
    }

}
