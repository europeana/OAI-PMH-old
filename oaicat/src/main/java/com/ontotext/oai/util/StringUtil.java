package com.ontotext.oai.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Patrick Ehlert on 9-1-18.
 */
public class StringUtil {

    private StringUtil() {
        // empty constructor
    }

    /**
     * Return the stacktrace as a 1 line string (so we can log it in ELK)
     * Note that this will filter out all new lines and all quotes as these mess up logging to ELK
     */
    public static String stacktraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String result = sw.toString();
        result.replaceAll("\"", "'");
        result.replaceAll("\n", " ^ ");
        return result;
    }

}
