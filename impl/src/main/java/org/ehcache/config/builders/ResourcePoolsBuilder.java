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

package org.ehcache.config.builders;

import org.ehcache.config.ResourcePool;
import org.ehcache.core.config.ResourcePoolImpl;
import org.ehcache.config.ResourcePools;
import org.ehcache.core.config.ResourcePoolsImpl;
import org.ehcache.config.ResourceType;
import org.ehcache.config.ResourceUnit;
import org.ehcache.config.units.MemoryUnit;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import java.util.HashMap;
import static org.ehcache.core.config.ResourcePoolsImpl.validateResourcePools;

/**
 * The {@code ResourcePoolsBuilder} enables building {@link ResourcePools} configurations using a fluent style.
 * <P>
 * As with all Ehcache builders, all instances are immutable and calling any method on the builder will return a new
 * instance without modifying the one on which the method was called.
 * This enables the sharing of builder instances without any risk of seeing them modified by code elsewhere.
 */
public class ResourcePoolsBuilder implements Builder<ResourcePools> {

  private final Map<ResourceType, ResourcePool> resourcePools;

  private ResourcePoolsBuilder() {
    this(Collections.<ResourceType, ResourcePool>emptyMap());
  }

  private ResourcePoolsBuilder(Map<ResourceType, ResourcePool> resourcePools) {
    validateResourcePools(resourcePools.values());
    this.resourcePools = unmodifiableMap(resourcePools);
  }

  /**
   * Creates a new {@code ResourcePoolsBuilder}.
   *
   * @return the new builder
   */
  public static ResourcePoolsBuilder newResourcePoolsBuilder() {
    return new ResourcePoolsBuilder();
  }

  /**
   * Convenience method to get a builder from an existing {@link ResourcePools}.
   *
   * @param pools the resource pools to build from
   * @return a new builder with configuration matching the provided resource pools
   */
  public static ResourcePoolsBuilder newResourcePoolsBuilder(ResourcePools pools) {
    ResourcePoolsBuilder poolsBuilder = new ResourcePoolsBuilder();
    for (ResourceType currentResourceType : pools.getResourceTypeSet()) {
      poolsBuilder = poolsBuilder.with(currentResourceType, pools.getPoolForResource(currentResourceType).getSize(),
          pools.getPoolForResource(currentResourceType).getUnit(), pools.getPoolForResource(currentResourceType).isPersistent());
    }
    return poolsBuilder;
  }

  /**
   * Adds or replace the {@link ResourcePool} of {@link ResourceType} in the returned builder.
   *
   * @param type the resource type
   * @param size the pool size
   * @param unit the pool size unit
   * @param persistent if the pool is to be persistent
   * @return a new builder with the added pool
   */
  public ResourcePoolsBuilder with(ResourceType type, long size, ResourceUnit unit, boolean persistent) {
    Map<ResourceType, ResourcePool> newPools = new HashMap<ResourceType, ResourcePool>(resourcePools);
    newPools.put(type, new ResourcePoolImpl(type, size, unit, persistent));
    return new ResourcePoolsBuilder(newPools);
  }

  /**
   * Convenience method to add a {@link org.ehcache.config.ResourceType.Core#HEAP} pool.
   *
   * @param size the pool size
   * @param unit the pool size unit
   * @return a new builder with the added pool
   */
  public ResourcePoolsBuilder heap(long size, ResourceUnit unit) {
    return with(ResourceType.Core.HEAP, size, unit, false);
  }

  /**
   * Convenience method to add a {@link org.ehcache.config.ResourceType.Core#OFFHEAP} pool.
   *
   * @param size the pool size
   * @param unit the pool size unit
   * @return a new builder with the added pool
   */
  public ResourcePoolsBuilder offheap(long size, MemoryUnit unit) {
    return with(ResourceType.Core.OFFHEAP, size, unit, false);
  }

  /**
   * Convenience method to add a non persistent {@link org.ehcache.config.ResourceType.Core#DISK} pool.
   *
   * @param size the pool size
   * @param unit the pool size unit
   * @return a new builder with the added pool
   */
  public ResourcePoolsBuilder disk(long size, MemoryUnit unit) {
    return disk(size, unit, false);
  }

  /**
   * Convenience method to add a {@link org.ehcache.config.ResourceType.Core#DISK} pool specifying persistence.
   *
   * @param size the pool size
   * @param unit the pool size unit
   * @param persistent if the pool is persistent or not
   * @return a new builder with the added pool
   */
  public ResourcePoolsBuilder disk(long size, MemoryUnit unit, boolean persistent) {
    return with(ResourceType.Core.DISK, size, unit, persistent);
  }

  /**
   * Builds the {@link ResourcePools} based on this builder's configuration.
   *
   * @return the resource pools
   */
  @Override
  public ResourcePools build() {
    return new ResourcePoolsImpl(resourcePools);
  }

}
