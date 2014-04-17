package com.ontotext.oai.util;

/**
 * Created by Simo on 16.4.2014 г..
 *
 * Helper class to implement remove for SimpleMap, having the index of the value.
 * Used to remove resumptionToken from the map when exhausted.
 */
public class NullifyObjectCallback implements Callback {
    private final Object[] objects;
    private final int[] removeIndices;

    public NullifyObjectCallback(Object[] objects, int []removeIndices) {
        this.objects = objects;
        this.removeIndices = removeIndices;
    }

    public void callback() {
        System.out.println("Remove resumption map");
        for (int i : removeIndices) {
            objects[i] = null;
        }
    }
}
