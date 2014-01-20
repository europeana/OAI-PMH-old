package com.ontotext.oai.server.crosswalk;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

/**
 * Created by Simo on 14-1-14.
 */
public class Edm2Rights extends Crosswalk {
    public Edm2Rights() {
        super("");
    }

    @Override
    public boolean isAvailableFor(Object nativeItem) {
        return false;
    }

    @Override
    public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException {
        return null;
    }
}
