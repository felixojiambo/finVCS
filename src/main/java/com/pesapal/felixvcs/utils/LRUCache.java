package com.pesapal.felixvcs.utils;

import com.pesapal.felixvcs.core.Blob;
import com.pesapal.felixvcs.core.Tree;

import java.util.LinkedHashMap;
import java.util.Map;

// Generic LRU Cache
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cacheMap;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // true for access-order to implement LRU
        this.cacheMap = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
                return size() > LRUCache.this.capacity;
            }
        };
    }

    public synchronized V get(K key) {
        return cacheMap.get(key);
    }

    public synchronized void put(K key, V value) {
        cacheMap.put(key, value);
    }
}
