package com.ontotext.oai.europeana.db.solr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * Created by Simo on 27.8.2014 г..
 */
public class HttpRetryHandler extends DefaultHttpRequestRetryHandler {
    private static final Log log = LogFactory.getLog(HttpRetryHandler.class);

    private static final int RETRY_COUNT = 5;
    private static final int MIN_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 15000;
    public HttpRetryHandler() {
        super(RETRY_COUNT, false);
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount > getRetryCount()) {
            log.info("Retry limit reached.");
        }

        if (super.retryRequest(exception, executionCount, context)) {
            sleep(executionCount);
            return true;
        }

        return false;
    }

    private static void sleep(int executionCount) {
        // line between 2 points (1, MIN_DELAY_MS) and (RETRY_COUNT, MAX_DELAY_MS)
        long sleepTime =
                ((RETRY_COUNT - executionCount) * MIN_DELAY_MS +
                (executionCount  - 1) * MAX_DELAY_MS)
                    / (RETRY_COUNT - 1);

        log.info("Retry/Sleep(" + sleepTime + ") : " +  executionCount + "/" + RETRY_COUNT);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.warn(e);
        }
    }

}
