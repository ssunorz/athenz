package com.yahoo.athenz.zts.store.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.yahoo.athenz.zts.cache.DataCache;
import com.yahoo.athenz.zts.store.CacheStoreFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class EhcacheCacheStoreFactory implements CacheStoreFactory {

    private static class EhcacheCache implements Cache<String, DataCache> {

        static final ConcurrentMap<String, DataCache> MAP = new ConcurrentHashMap<>();

        final org.ehcache.Cache<String, DataCache> ehcache;

        public EhcacheCache(org.ehcache.Cache<String, DataCache> ehcache) {
            this.ehcache = ehcache;
        }

        @Override
        public @Nullable DataCache getIfPresent(Object key) {
            return ehcache.get((String) key);
        }

        @Override
        public DataCache get(String key, Callable<? extends DataCache> loader) throws ExecutionException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableMap<String, DataCache> getAllPresent(Iterable<?> keys) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(String key, DataCache value) {
            ehcache.put(key, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends DataCache> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void invalidate(Object key) {
            ehcache.remove((String) key);
        }

        @Override
        public void invalidateAll(Iterable<?> keys) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void invalidateAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CacheStats stats() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConcurrentMap<String, DataCache> asMap() {
            MAP.clear();
            ehcache.iterator().forEachRemaining(entry -> MAP.put(entry.getKey(), entry.getValue()));
            return MAP;
        }

        @Override
        public void cleanUp() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Cache<String, DataCache> create() {
        String alias = "ehcache";
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File("")))
                .withCache(alias,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DataCache.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, EntryUnit.ENTRIES)
                                        .disk(1, MemoryUnit.GB, true)
                        )
                ).build(true);

        org.ehcache.Cache<String, DataCache> ehcache =
                persistentCacheManager.getCache(alias, String.class, DataCache.class);

        return new EhcacheCache(ehcache);
    }
}