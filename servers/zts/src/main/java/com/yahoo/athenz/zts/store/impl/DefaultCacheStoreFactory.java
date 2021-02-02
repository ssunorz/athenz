package com.yahoo.athenz.zts.store.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yahoo.athenz.zts.cache.DataCache;
import com.yahoo.athenz.zts.store.CacheStoreFactory;

public class DefaultCacheStoreFactory implements CacheStoreFactory {
    @Override
    public Cache<String, DataCache> create() {
        return CacheBuilder.newBuilder().concurrencyLevel(25).build();
    }
}
