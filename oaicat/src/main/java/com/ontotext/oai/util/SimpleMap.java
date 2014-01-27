package com.ontotext.oai.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Simo on 14-1-21.
 */
public class SimpleMap <K, V> implements Map<K, V>{
    private final K[] keys;
    private final V[] vals;
    private final int size;
    public SimpleMap(K []keys, V[] vals) {
        this.keys = keys;
        this.vals = vals;
        this.size = keys.length;
    }

    private int locateKey(Object key) {
        int index = -1;
        for (int i = 0; i != keys.length; ++i) {
            if (key.equals(keys[i])) {
                index = i;
                break;
            }
        }

        return index;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size != 0;
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public V get(Object key) {
        int index = locateKey(key);
        if (index >= 0) {
            return vals[index];
        }
        return null;
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
