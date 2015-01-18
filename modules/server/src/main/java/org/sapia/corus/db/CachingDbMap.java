package org.sapia.corus.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.Persistent;

/**
 * A {@link DbMap} implementation that does LRU-based caching.
 * 
 * @author yduchesne
 * 
 */
public class CachingDbMap<K, V> implements DbMap<K, V> {

  private static final int DEFAULT_MAX_SIZE = 100;

  private DbMap<K, V> delegate;
  private LinkedHashMap<K, V> cache;

  /**
   * @param delegate
   *          the {@link DbMap} on top of which to do caching.
   * @param maxSize
   *          the maximum size of the cache, based on which entries are
   *          internally evicted.
   */
  public CachingDbMap(DbMap<K, V> delegate, final int maxSize) {
    this.delegate = delegate;

    cache = new LinkedHashMap<K, V>(50, 0.75f, true) {
      static final long serialVersionUID = 1L;

      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= maxSize;
      }
    };
  }

  /**
   * Constructs an instance of this class with a max size of 50.
   * 
   * @param delegate
   *          the {@link DbMap} on top of which to do caching.
   */
  public CachingDbMap(DbMap<K, V> delegate) {
    this(delegate, DEFAULT_MAX_SIZE);
  }

  @Override
  public synchronized void clear() {
    cache.clear();
    delegate.clear();
  }

  @Override
  public synchronized void close() {
    delegate.close();
  }

  @Override
  public RecordMatcher<V> createMatcherFor(V template) {
    return delegate.createMatcherFor(template);
  }

  @Override
  public synchronized V get(K key) {
    V item = cache.get(key);
    if (item == null) {
      item = delegate.get(key);
    }
    if (item != null) {
      delegate.put(key, item);
    }
    return item;
  }

  @Override
  public ClassDescriptor<V> getClassDescriptor() {
    return delegate.getClassDescriptor();
  }

  @Override
  public Iterator<K> keys() {
    return delegate.keys();
  }

  @Override
  public synchronized void put(K key, V value) {
    cache.put(key, value);
    delegate.put(key, value);
  }

  @Override
  public synchronized void refresh(K key, V value) {
    delegate.refresh(key, value);
    cache.put(key, value);
  }

  @Override
  public synchronized void remove(K key) {
    V value = get(key);
    if (value != null && value instanceof Persistent<?, ?>) {
      ((Persistent<?, ?>) value).markDeleted();
    }
    cache.remove(key);
    delegate.remove(key);
  }

  @Override
  public Iterator<V> values() {
    return new CacheIterator(delegate.values());
  }
  
  @Override
  public Iterator<V> iterator() {
    return values();
  }

  @Override
  public Collection<V> values(RecordMatcher<V> matcher) {
    Collection<V> values = delegate.values(matcher);
    Collection<V> toReturn = new ArrayList<V>(values.size());

    for (V val : values) {
      toReturn.add(getCachedValue(val));
    }
    return toReturn;
  }

  @SuppressWarnings(value = "unchecked")
  private V getCachedValue(V val) {
    if (val instanceof Persistent) {
      Persistent<K, V> p = (Persistent<K, V>) val;
      K key = p.getKey();
      V cached = cache.get(key);
      if (cached != null) {
        return cached;
      } else {
        return val;
      }
    } else {
      return val;
    }
  }

  class CacheIterator implements Iterator<V> {

    Iterator<V> delegateIterator;

    public CacheIterator(Iterator<V> delegateIterator) {
      this.delegateIterator = delegateIterator;
    }

    @Override
    public boolean hasNext() {
      return delegateIterator.hasNext();
    }

    @Override
    public V next() {
      V next = delegateIterator.next();
      return getCachedValue(next);
    }

    @Override
    public void remove() {
    }

  }

}
