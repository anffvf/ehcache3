package org.ehcache.internal.tier;

import org.ehcache.exceptions.CacheAccessException;
import org.ehcache.expiry.Expirations;
import org.ehcache.function.Function;
import org.ehcache.spi.cache.Store;
import org.ehcache.spi.cache.tiering.CachingTier;
import org.ehcache.spi.test.After;
import org.ehcache.spi.test.Before;
import org.ehcache.spi.test.SPITest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Test the {@link CachingTier#remove(K key)} contract of the
 * {@link CachingTier CachingTier} interface.
 * <p/>
 *
 * @author Aurelien Broszniowski
 */
public class CachingTierRemove<K, V> extends CachingTierTester<K, V> {

  private CachingTier tier;

  public CachingTierRemove(final CachingTierFactory<K, V> factory) {
    super(factory);
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
    if (tier != null) {
      tier.close();
      tier = null;
    }
  }

  @SPITest
  @SuppressWarnings("unchecked")
  public void removeMapping() {
    K key = factory.createKey(1);

    final Store.ValueHolder<V> valueHolder = mock(Store.ValueHolder.class);

    tier = factory.newCachingTier(factory.newConfiguration(factory.getKeyType(), factory.getValueType(),
        1L, null, null, Expirations.noExpiration()));

    try {
      tier.getOrComputeIfAbsent(key, new Function<K, Store.ValueHolder<V>>() {
        @Override
        public Store.ValueHolder<V> apply(final K k) {
          return valueHolder;
        }
      });

      tier.remove(key);

      final Store.ValueHolder<V> newValueHolder = mock(Store.ValueHolder.class);
      Store.ValueHolder<V> newReturnedValueHolder = tier.getOrComputeIfAbsent(key, new Function() {
        @Override
        public Object apply(final Object o) {
          return newValueHolder;
        }
      });

      assertThat(newReturnedValueHolder, is(equalTo(newValueHolder)));
    } catch (CacheAccessException e) {
      System.err.println("Warning, an exception is thrown due to the SPI test");
      e.printStackTrace();
    }
  }
}
