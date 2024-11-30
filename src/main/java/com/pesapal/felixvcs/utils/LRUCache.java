package com.pesapal.felixvcs.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A generic implementation of a Least Recently Used (LRU) cache.
 * <p>
 * This cache uses a {@link LinkedHashMap} with access-order to maintain the
 * least recently used eviction policy.
 *
 * @param <K> The type of keys maintained by this cache.
 * @param <V> The type of mapped values.
 */
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cacheMap;

    /**
     * Constructs an LRUCache with the specified capacity.
     *
     * @param capacity The maximum number of entries the cache can hold.
     */
    public LRUCache(int capacity) {
        this.capacity = capacity;

        // Initialize LinkedHashMap with access-order for LRU eviction.
        this.cacheMap = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                // Remove the eldest entry when the size exceeds the capacity.
                return size() > LRUCache.this.capacity;
            }
        };
    }

    /**
     * Retrieves a value associated with the specified key.
     * Accessing an entry updates its access order in the cache.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the key, or {@code null} if the key does not exist.
     */
    public synchronized V get(K key) {
        return cacheMap.get(key);
    }

    /**
     * Inserts a key-value pair into the cache.
     * If the key already exists, its value is updated, and its access order is refreshed.
     * If the cache exceeds its capacity, the least recently used entry is evicted.
     *
     * @param key   The key to insert or update in the cache.
     * @param value The value to associate with the key.
     */
    public synchronized void put(K key, V value) {
        cacheMap.put(key, value);
    }

    /**
     * Returns the current number of entries in the cache.
     *
     * @return The size of the cache.
     */
    public synchronized int size() {
        return cacheMap.size();
    }

    /**
     * Checks whether the cache contains a mapping for the specified key.
     *
     * @param key The key to check for.
     * @return {@code true} if the cache contains a mapping for the key, {@code false} otherwise.
     */
    public synchronized boolean containsKey(K key) {
        return cacheMap.containsKey(key);
    }

    /**
     * Clears all entries from the cache.
     */
    public synchronized void clear() {
        cacheMap.clear();
    }

    /**
     * Returns a string representation of the cache for debugging purposes.
     *
     * @return A string representing the cache's contents.
     */
    @Override
    public synchronized String toString() {
        return cacheMap.toString();
    }
}
