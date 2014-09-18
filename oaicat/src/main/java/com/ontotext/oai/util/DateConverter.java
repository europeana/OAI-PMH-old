package com.ontotext.oai.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by Simo on 14-1-22.
 */
public class DateConverter {
    private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    private static final DateTimeFormatter fmt2 = ISODateTimeFormat.dateTime().withZoneUTC();

    public static Date fromIsoDateTime(String isoDateTime) {
        return new DateTime(isoDateTime).toDate();
    }

    /**
     * Converts Date to string in the OAI-PMH format: YYYY-MM-DDThh:mm:ssZ
     * @param date
     * @return UTC Date string with 1 second granularity
     */
    public static String toIsoDate(Date date) {
        return fmt.print(new DateTime(date));
    }

    /**
     * Converts Date to UTC date string (YYYY-MM-DDThh:mm:ss.SSSZ).
     * @param date
     * @return UTC Date string with 1 millisecond granularity
     */
    public static String toIsoDate2(Date date) {
        return fmt2.print(new DateTime(date));
    }
}
