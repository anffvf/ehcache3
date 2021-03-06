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

package org.ehcache.core.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.config.Configuration;
import org.ehcache.config.RuntimeConfiguration;
import org.ehcache.spi.service.ServiceCreationConfiguration;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

/**
 * Base implementation of {@link Configuration}.
 */
public final class DefaultConfiguration implements Configuration, RuntimeConfiguration {

  private final ConcurrentMap<String,CacheConfiguration<?, ?>> caches;
  private final Collection<ServiceCreationConfiguration<?>> services;
  private final ClassLoader classLoader;

  /**
   * Copy constructor
   *
   * @param cfg the configuration to copy
   */
  public DefaultConfiguration(Configuration cfg) {
    this.caches = new ConcurrentHashMap<String, CacheConfiguration<?, ?>>(cfg.getCacheConfigurations());
    this.services = unmodifiableCollection(cfg.getServiceCreationConfigurations());
    this.classLoader = cfg.getClassLoader();
  }

  /**
   * Creates a new configuration with the specified class loader.
   * <P>
   *   This means no cache configurations nor service configurations.
   * </P>
   *
   * @param classLoader the class loader to use
   * @param services an array of service configurations
   *
   * @see #addCacheConfiguration(String, CacheConfiguration)
   */
  public DefaultConfiguration(ClassLoader classLoader, ServiceCreationConfiguration<?>... services) {
    this(emptyCacheMap(), classLoader, services);
  }

  /**
   * Creates a new configuration with the specified {@link CacheConfiguration cache configurations}, class loader and
   * {@link org.ehcache.spi.service.ServiceConfiguration service configurations}.
   *
   * @param caches a map from alias to cache configuration
   * @param classLoader the class loader to use for user types
   * @param services an array of service configurations
   */
  public DefaultConfiguration(Map<String, CacheConfiguration<?, ?>> caches, ClassLoader classLoader, ServiceCreationConfiguration<?>... services) {
    this.services = unmodifiableCollection(Arrays.asList(services));
    this.caches = new ConcurrentHashMap<String, CacheConfiguration<?, ?>>(caches);
    this.classLoader = classLoader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, CacheConfiguration<?, ?>> getCacheConfigurations() {
    return unmodifiableMap(caches);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<ServiceCreationConfiguration<?>> getServiceCreationConfigurations() {
    return services;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  private static Map<String, CacheConfiguration<?, ?>> emptyCacheMap() {
    return Collections.emptyMap();
  }

  /**
   * Adds a {@link CacheConfiguration} tied to the provided alias.
   *
   * @param alias the alias of the cache
   * @param config the configuration of the cache
   */
  public void addCacheConfiguration(final String alias, final CacheConfiguration<?, ?> config) {
    if (caches.put(alias, config) != null) {
      throw new IllegalStateException("Cache '" + alias + "' already present!");
    }
  }

  /**
   * Removes the {@link CacheConfiguration} tied to the provided alias.
   *
   * @param alias the alias for which to remove configuration
   *
   * @throws IllegalStateException if the alias was not in use
   */
  public void removeCacheConfiguration(final String alias) {
    if (caches.remove(alias) == null) {
      throw new IllegalStateException("Cache '" + alias + "' unknown!");
    }
  }

  /**
   * Replaces a {@link CacheConfiguration} with a {@link CacheRuntimeConfiguration} for the provided alias.
   *
   * @param alias the alias of the cache
   * @param config the existing configuration
   * @param runtimeConfiguration the new configuration
   * @param <K> the key type
   * @param <V> the value type
   *
   * @throws IllegalStateException if the replace fails
   */
  public <K, V> void replaceCacheConfiguration(final String alias, final CacheConfiguration<K, V> config, final CacheRuntimeConfiguration<K, V> runtimeConfiguration) {
    if (!caches.replace(alias, config, runtimeConfiguration)) {
      throw new IllegalStateException("The expected configuration doesn't match!");
    }
  }
}
