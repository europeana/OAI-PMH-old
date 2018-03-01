package com.ontotext.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Simo on 14-1-30.
 */
public final class QueryListIdentifiers extends BaseListQuery {

    private QueryListIdentifiers(String from, String until, String set, String prefix) {
        super(from, until, set, prefix);
    }

    public static List<QueryListIdentifiers> loadMultiple(Properties properties) {
        List<QueryListIdentifiers> result = new ArrayList<>();

        String from = properties.getProperty("QueryListIdentifiers.from");
        String until = properties.getProperty("QueryListIdentifiers.until");
        String set = properties.getProperty("QueryListIdentifiers.set");

        if (from == null && until == null && set == null) {
            return result;
        }

        String prefix = properties.getProperty("QueryListIdentifiers.prefix", "edm");

        String[] sets = set.split(",");
        for (String s : sets) {
            result.add(new QueryListIdentifiers(from, until, s, prefix));
        }
        return result;
    }

}
