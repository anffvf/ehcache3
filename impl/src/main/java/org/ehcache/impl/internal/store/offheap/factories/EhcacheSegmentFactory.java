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

package org.ehcache.impl.internal.store.offheap.factories;

import org.ehcache.config.EvictionVeto;

import org.terracotta.offheapstore.Metadata;
import org.terracotta.offheapstore.ReadWriteLockedOffHeapClockCache;
import org.terracotta.offheapstore.paging.PageSource;
import org.terracotta.offheapstore.pinning.PinnableSegment;
import org.terracotta.offheapstore.storage.StorageEngine;
import org.terracotta.offheapstore.util.Factory;

import java.util.concurrent.locks.Lock;

/**
 * EhcacheSegmentFactory
 */
public class EhcacheSegmentFactory<K, V> implements Factory<PinnableSegment<K, V>> {

  private final Factory<? extends StorageEngine<? super K, ? super V>> storageEngineFactory;
  private final PageSource tableSource;
  private final int tableSize;
  private final EvictionVeto<? super K, ? super V> evictionVeto;
  private final EhcacheSegment.EvictionListener<K, V> evictionListener;

  public EhcacheSegmentFactory(PageSource source, Factory<? extends StorageEngine<? super K, ? super V>> storageEngineFactory, int initialTableSize, EvictionVeto<? super K, ? super V> evictionVeto, EhcacheSegment.EvictionListener<K, V> evictionListener) {
    this.storageEngineFactory = storageEngineFactory;
    this.tableSource = source;
    this.tableSize = initialTableSize;
    this.evictionVeto = evictionVeto;
    this.evictionListener = evictionListener;
  }

  public PinnableSegment<K, V> newInstance() {
    StorageEngine<? super K, ? super V> storageEngine = storageEngineFactory.newInstance();
    try {
      return new EhcacheSegment<K, V>(tableSource, storageEngine, tableSize, evictionVeto, evictionListener);
    } catch (RuntimeException e) {
      storageEngine.destroy();
      throw e;
    }
  }

  public static class EhcacheSegment<K, V> extends ReadWriteLockedOffHeapClockCache<K, V> {

    public static final int VETOED = 1 << (Integer.SIZE - 3);

    private final EvictionVeto<? super K, ? super V> evictionVeto;
    private final EvictionListener<K, V> evictionListener;

    EhcacheSegment(PageSource source, StorageEngine<? super K, ? super V> storageEngine, int tableSize, EvictionVeto<? super K, ? super V> evictionVeto, EvictionListener<K, V> evictionListener) {
      super(source, true, storageEngine, tableSize);
      this.evictionVeto = evictionVeto;
      this.evictionListener = evictionListener;
    }

    @Override
    public V put(K key, V value) {
      int metadata = getVetoedStatus(key, value);
      return put(key, value, metadata);
    }

    private int getVetoedStatus(final K key, final V value) {
      return evictionVeto.vetoes(key, value) ? VETOED : 0;
    }

    @Override
    public V putPinned(K key, V value) {
      int metadata = getVetoedStatus(key, value) | Metadata.PINNED;
      return put(key, value, metadata);
    }

    @Override
    protected boolean evictable(int status) {
      return super.evictable(status) && ((status & VETOED) == 0);
    }

    @Override
    public boolean evict(int index, boolean shrink) {
      Lock lock = writeLock();
      lock.lock();
      try {
        Entry<K, V> entry = getEntryAtTableOffset(index);
        boolean evicted = super.evict(index, shrink);
        if (evicted) {
          evictionListener.onEviction(entry.getKey(), entry.getValue());
        }
        return evicted;
      } finally {
        lock.unlock();
      }
    }

    public interface EvictionListener<K, V> {
      void onEviction(K key, V value);
    }
  }
}
