package com.ontotext.oai.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by Simo on 14-1-22.
 */
public class DateConverter {
    private final DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    public Date fromIsoDateTime(String isoDateTime) {
        return new DateTime(isoDateTime).toDate();
    }

    public String toIsoDate(Date date) {
        return fmt.print(new DateTime(date));
    }

    public static void main(String[] args) {
        DateConverter converter = new DateConverter();
        Date date = converter.fromIsoDateTime("2014-01-15T08:26:57.600Z");
        System.out.println(date);
        String reversed = converter.toIsoDate(date);
        System.out.println(reversed);
    }
}
