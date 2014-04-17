package com.ontotext.oai.server.iterator;

import ORG.oclc.oai.server.catalog.RecordFactory;
import com.ontotext.oai.ResumptionToken;
import com.ontotext.oai.europeana.RegistryInfo;

/**
 * Created by Simo on 16.4.2014 г..
 */
public class HeadersIterator extends ResumptionTokenIterator {
    public HeadersIterator(ResumptionToken token, RecordFactory recordFactory, int limit) {
        super(token,  recordFactory, limit);
    }

    String convert(RegistryInfo registryInfo) {
        System.out.println("Headers");
        StringBuilder sb = new StringBuilder(500);
        if (registryInfo != null) {
            sb.append("<header>");
            String id = recordFactory.getOAIIdentifier(registryInfo);
            if (id != null) {
                sb.append("<identifier>").append(id).append("</identifier>");
            }

            String datestamp = recordFactory.getDatestamp(registryInfo);
            if (datestamp != null) {
                sb.append("<datestamp>").append(datestamp).append("</datestamp>");
            }

            String setSpec = registryInfo.cid;
            if (setSpec != null) {
                sb.append("<setSpec>").append(setSpec).append("</setSpec>");
            }
            sb.append("</header>");
        }

        return sb.toString();
    }
}
