package com.yahoo.athenz.zts.store.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.yahoo.athenz.zts.cache.DataCache;
import com.yahoo.athenz.zts.store.CacheStoreFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class EhcacheCacheStoreFactory implements CacheStoreFactory {

    @Override
    public Cache<String, DataCache> create() {
        return new Cache<String, DataCache>() {
            @Override
            public @Nullable DataCache getIfPresent(Object key) {
                return null;
            }

            @Override
            public DataCache get(String key, Callable<? extends DataCache> loader) throws ExecutionException {
                return null;
            }

            @Override
            public ImmutableMap<String, DataCache> getAllPresent(Iterable<?> keys) {
                return null;
            }

            @Override
            public void put(String key, DataCache value) {

            }

            @Override
            public void putAll(Map<? extends String, ? extends DataCache> m) {

            }

            @Override
            public void invalidate(Object key) {

            }

            @Override
            public void invalidateAll(Iterable<?> keys) {

            }

            @Override
            public void invalidateAll() {

            }

            @Override
            public long size() {
                return 0;
            }

            @Override
            public CacheStats stats() {
                return null;
            }

            @Override
            public ConcurrentMap<String, DataCache> asMap() {
                return null;
            }

            @Override
            public void cleanUp() {

            }
        };
    }
}