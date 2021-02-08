package com.yahoo.athenz.zts.store.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.yahoo.athenz.zms.*;
import com.yahoo.athenz.zts.cache.DataCache;
import com.yahoo.athenz.zts.cache.MemberRole;
import com.yahoo.athenz.zts.store.CacheStoreFactory;
import com.yahoo.rdl.Timestamp;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    public static class DataCacheSerializer implements Serializer<DataCache> {

        private final Kryo kryo = new Kryo();

        private void regist() {
            kryo.register(HashMap.class);
            kryo.register(HashSet.class);
            kryo.register(ArrayList.class);

            kryo.register(DataCache.class);
            kryo.register(DomainData.class);
            kryo.register(MemberRole.class);
            kryo.register(RoleMeta.class);

            kryo.register(Role.class);
            kryo.register(StringList.class);
            kryo.register(RoleMember.class);
            kryo.register(RoleAuditLog.class);

            kryo.register(SignedPolicies.class);
            kryo.register(DomainPolicies.class);
            kryo.register(Policy.class);
            kryo.register(Assertion.class);

            kryo.register(ServiceIdentity.class);
            kryo.register(PublicKeyEntry.class);
            kryo.register(Entity.class);
            kryo.register(Group.class);
            kryo.register(GroupMember.class);
            kryo.register(GroupAuditLog.class);

            kryo.register(Timestamp.class);

            kryo.register(AssertionEffect.class);
        }

        public DataCacheSerializer(ClassLoader cl) {
            regist();
        }

        @Override
        public ByteBuffer serialize(DataCache dataCache) throws SerializerException {
            try (Output output = new Output(new ByteArrayOutputStream())) {
                kryo.writeObject(output, dataCache);
                return ByteBuffer.wrap(output.getBuffer());
            }
        }

        @Override
        public DataCache read(ByteBuffer byteBuffer) throws ClassNotFoundException, SerializerException {
            try (Input input = new Input(new ByteBufferInputStream(byteBuffer))) {
                return kryo.readObject(input, DataCache.class);
            }
        }

        @Override
        public boolean equals(DataCache dataCache, ByteBuffer byteBuffer) throws ClassNotFoundException, SerializerException {
            return dataCache.equals(read(byteBuffer));
        }
    }

    private static final String ALIAS = "ehcache";
    private static final String KEY_PREFIX = "athenz.zts.ehcache_store.";

    private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheCacheStoreFactory.class);

    @Override
    public Cache<String, DataCache> create() {
        String rootDirectory = System.getProperty(KEY_PREFIX + "persistence_dir");
        long heapSize = Long.parseLong(System.getProperty(KEY_PREFIX + "cache_heap_entries"));
        long diskSize = Long.parseLong(System.getProperty(KEY_PREFIX + "cache_disk_gb"));

        LOGGER.info("rootDirectory: {}, heapSize: {}, diskSize: {}", rootDirectory, heapSize, diskSize);

        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(rootDirectory))
                .withCache(ALIAS,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DataCache.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(heapSize, EntryUnit.ENTRIES)
                                        .disk(diskSize, MemoryUnit.GB, true)
                        ).withValueSerializer(DataCacheSerializer.class)
                ).build(true);

        org.ehcache.Cache<String, DataCache> ehcache =
                persistentCacheManager.getCache(ALIAS, String.class, DataCache.class);

        return new EhcacheCache(ehcache);
    }
}
