package com.ontotext.oai.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Simo on 16.4.2014 г..
 *
 * Helper class to implement remove for SimpleMap, having the index of the value.
 * Used to remove resumptionToken from the map when exhausted.
 */
public class NullifyObjectCallback implements Callback {
    private final Log log = LogFactory.getLog(NullifyObjectCallback.class);
    private final Object[] objects;
    private final int[] removeIndices;

    public NullifyObjectCallback(Object[] objects, int []removeIndices) {
        this.objects = objects;
        this.removeIndices = removeIndices;
    }

    public void callback() {
        log.debug("Remove resumption map");
        for (int i : removeIndices) {
            objects[i] = null;
        }
    }
}
