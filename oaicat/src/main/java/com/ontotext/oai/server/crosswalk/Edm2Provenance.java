package com.ontotext.oai.server.crosswalk;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.ontotext.oai.RecordInfo;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Simo on 14-1-14.
 */
public class Edm2Provenance extends Crosswalk {

    public Edm2Provenance() {
        super("http://www.openarchives.org/OAI/2.0/provenance http://www.openarchives.org/OAI/2.0/provenance.xsd");
    }

    @Override
    public boolean isAvailableFor(Object nativeItem) {
        if (nativeItem instanceof RecordInfo) {
            return true;
        }
        return false;
    }

    @Override
    public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException {
        String provenance = null;
        if (nativeItem instanceof RecordInfo) {
            int numItems = 0;

            RecordInfo recordInfo = (RecordInfo) nativeItem;
            if (recordInfo.registryInfo != null) {
                StringBuilder sb = new StringBuilder(1000);
                sb.append("<provenance xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/provenance" +
                        " http://www.openarchives.org/OAI/2.0/provenance.xsd\">");
                sb.append("<originDescription>");
                String originalId = recordInfo.getOriginalId();
                if (originalId != null) {
                    try {
                        URL url = new URL(originalId);
                        sb.append("<baseURL>");
                        sb.append(url.getHost());
                        sb.append("</baseURL>");
                        sb.append("<identifier>");
                        sb.append(originalId);
                        sb.append("</identifier>");
                        ++numItems;
                    } catch (MalformedURLException e) {
                        throw new CannotDisseminateFormatException("edm");
                    }
                }


                sb.append("</originDescription>");
                sb.append("</provenance>");
                if (numItems != 0) {
                    provenance = sb.toString();
                }
            }

        }
        return provenance;
    }
}
