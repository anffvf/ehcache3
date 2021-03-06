/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.core.internal.store;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.EvictionVeto;
import org.ehcache.config.ResourcePools;
import org.ehcache.expiry.Expiry;
import org.ehcache.core.spi.store.Store;
import org.ehcache.spi.serialization.Serializer;

/**
 * Implementation of the {@link org.ehcache.core.spi.store.Store.Configuration store configuration interface} as used by
 * {@link org.ehcache.core.EhcacheManager EhcacheManager} in order to prepare {@link Store} creation.
 */
public class StoreConfigurationImpl<K, V> implements Store.Configuration<K, V> {

  private final Class<K> keyType;
  private final Class<V> valueType;
  private final EvictionVeto<? super K, ? super V> evictionVeto;
  private final ClassLoader classLoader;
  private final Expiry<? super K, ? super V> expiry;
  private final ResourcePools resourcePools;
  private final Serializer<K> keySerializer;
  private final Serializer<V> valueSerializer;
  private final int orderedEventParallelism;

  /**
   * Creates a new {@code StoreConfigurationImpl} based on the provided parameters.
   *
   * @param cacheConfig the cache configuration
   * @param orderedEventParallelism the level of parallelism for ordered events
   * @param keySerializer the key serializer
   * @param valueSerializer the value serializer
   */
  public StoreConfigurationImpl(CacheConfiguration<K, V> cacheConfig, int orderedEventParallelism,
                                Serializer<K> keySerializer, Serializer<V> valueSerializer) {
    this(cacheConfig.getKeyType(), cacheConfig.getValueType(), cacheConfig.getEvictionVeto(),
        cacheConfig.getClassLoader(), cacheConfig.getExpiry(), cacheConfig.getResourcePools(),
        orderedEventParallelism, keySerializer, valueSerializer);
  }

  /**
   * Creates a new {@code StoreConfigurationImpl} based on the provided parameters.
   *
   * @param keyType the key type
   * @param valueType the value type
   * @param evictionVeto the eviction veto
   * @param classLoader the class loader
   * @param expiry the expiry policy
   * @param resourcePools the resource pools
   * @param orderedEventParallelism the level or parallelism for ordered events
   * @param keySerializer the key serializer
   * @param valueSerializer the value serializer
   */
  public StoreConfigurationImpl(Class<K> keyType, Class<V> valueType,
                                EvictionVeto<? super K, ? super V> evictionVeto,
                                ClassLoader classLoader, Expiry<? super K, ? super V> expiry,
                                ResourcePools resourcePools, int orderedEventParallelism,
                                Serializer<K> keySerializer, Serializer<V> valueSerializer) {
    this.keyType = keyType;
    this.valueType = valueType;
    this.evictionVeto = evictionVeto;
    this.classLoader = classLoader;
    this.expiry = expiry;
    this.resourcePools = resourcePools;
    this.keySerializer = keySerializer;
    this.valueSerializer = valueSerializer;
    this.orderedEventParallelism = orderedEventParallelism;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<K> getKeyType() {
    return keyType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<V> getValueType() {
    return valueType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EvictionVeto<? super K, ? super V> getEvictionVeto() {
    return evictionVeto;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClassLoader getClassLoader() {
    return this.classLoader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Expiry<? super K, ? super V> getExpiry() {
    return expiry;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourcePools getResourcePools() {
    return resourcePools;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Serializer<K> getKeySerializer() {
    return keySerializer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Serializer<V> getValueSerializer() {
    return valueSerializer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getOrderedEventParallelism() {
    return orderedEventParallelism;
  }
}
