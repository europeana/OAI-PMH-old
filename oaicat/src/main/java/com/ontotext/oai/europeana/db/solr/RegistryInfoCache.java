package com.ontotext.oai.europeana.db.solr;

import com.ontotext.oai.europeana.RegistryInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Simo on 16.10.2014 г..
 */
public class RegistryInfoCache {
    private static final Log log = LogFactory.getLog(RegistryInfoCache.class);

    private static final long CLEANUP_MINUTES = 1L;
    private static final long CLEANUP_MILLISECONDS = CLEANUP_MINUTES*60L*1000L;
    private static final int MAX_COUNT = 10;

    private static final int MIN_CACHE_SIZE = 2;
    private static final int DEFAULT_CACHE_SIZE = 10;

    private SolrRegistry.QueryIterator[] iterators;
    private int[] counters;
    int size = 0;
    int capacity;
    private final Lock readLock;
    private final Lock writeLock;

    public RegistryInfoCache() {
        this(DEFAULT_CACHE_SIZE);
    }

    public RegistryInfoCache(int initialCapacity) {
        if (initialCapacity < MIN_CACHE_SIZE) {
            initialCapacity = MIN_CACHE_SIZE;
        }
        capacity = initialCapacity;
        iterators = new SolrRegistry.QueryIterator[initialCapacity];
        counters = new int[initialCapacity];

        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        scheduleCleanupThread();
    }

    public RegistryInfo get(String recordId) {
        try {
            readLock.lock();
            for (int i = 0; i != size; ++i) {
                SolrRegistry.QueryIterator queryIterator = iterators[i];
                if (queryIterator == null) {
                    break;
                }
                RegistryInfo registryInfo = queryIterator.last();
                if (registryInfo != null && registryInfo.eid.equals(recordId)) {
                    // No needed write lock - only one thread should modify one counter.
                    // The cleanup thread holds write lock so race is not possible.
                    counters[i] = 0; // reset counter.
                    return registryInfo;
                }
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public SolrRegistry.QueryIterator add(SolrRegistry.QueryIterator it) {
        try {
            writeLock.lock();
            if (size == capacity) {
                grow();
            }
            iterators[size++] = it;
            return it;
        } finally {
            writeLock.unlock();
        }
    }

    private void grow() {
        int newCapacity = (capacity * 3) / 2;

        SolrRegistry.QueryIterator[] newIt = new SolrRegistry.QueryIterator[newCapacity];
        System.arraycopy(iterators, 0, newIt, 0, capacity);
        iterators = newIt;

        int[] newCounters = new int[newCapacity];
        System.arraycopy(counters, 0, newCounters,  0, counters.length);
        counters = newCounters;

        capacity = newCapacity;
    }

    // shift next over deleted element
    private void erase(int i) {
        if (log.isDebugEnabled()) {
            log.debug("Remove cache[" + i + "]");
        }
        for (int j = i + 1; j < size; ++j, ++i) {
            iterators[i] = iterators[j];
            counters[i] = counters[j];
        }

        iterators[i] = null;
        counters[i] = 0;
        --size;
    }

    /**
     * Create daemon thread to check every minute for expired tokens and remove them from map.
     */
    private void scheduleCleanupThread() {
        Timer timer = new Timer("Clean2", true); // isDaemon
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.debug("Cleanup cache thread.");
                try {
                    writeLock.lock();
                    for (int i = size; --i >= 0;) {
                        if (++counters[i] == MAX_COUNT) {
                            erase(i);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }, CLEANUP_MILLISECONDS, CLEANUP_MILLISECONDS);
    }
}
