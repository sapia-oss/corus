package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RecordMatcher;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Persistent;
import org.sapia.ubik.util.Strings;

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

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public synchronized V get(K key) {
    V item = cache.get(key);
    if (item == null) {
      item = delegate.get(key);
    }
    if (item instanceof Persistent) {
      Persistent p = (Persistent) item;
      p.setDbMap(this);
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

  @SuppressWarnings({ "unchecked"})
  @Override
  public synchronized void put(K key, V value) {
    cache.put(key, value);
    delegate.put(key, value);
    
    if (value instanceof Persistent) {
      ((Persistent<K, V>) value).setDbMap(this);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void refresh(K key, V value) {
    delegate.refresh(key, value);
    cache.put(key, value);
    
    if (value instanceof Persistent) {
      ((Persistent<K, V>) value).setDbMap(this);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void remove(K key) {
    V value = get(key);
    if (value != null && value instanceof Persistent) {
      ((Persistent<K, V>) value).markDeleted();
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
      toReturn.add(substituteForCachedValue(val));
    }
    return toReturn;
  }
  
  @Override
  public void archive(RevId revId, K key) {
    cache.remove(key);
    delegate.archive(revId, key);
  }
  
  @Override
  public void archive(RevId revId, Collection<K> keys) {
    for (K k : keys) {
      cache.remove(k);
    }
    delegate.archive(revId, keys);
  }
  
  public List<K> unarchive(RevId revId) {
    List<K> results = delegate.unarchive(revId);
    for (K k : results) {
      cache.remove(k);
    }
    return results;
  }
  
  @Override
  public void clearArchive(RevId revId) {
    delegate.clearArchive(revId);
  }
  
  Map<K, V> getInternalMap() {
    return this.cache;
  }

  @SuppressWarnings(value = { "unchecked", "rawtypes" })
  private V substituteForCachedValue(V val) {
    if (val instanceof Persistent) {
      Persistent<K, V> p = (Persistent<K, V>) val;
      K key = p.getKey();
      V cached = cache.get(key);
      if (cached != null) {
        return cached;
      } else {
        cache.put(key, val);
        ((Persistent) val).setDbMap(this);
         return val;
      }
    } else {
      return val;
    }
  }
  
  @Override
  public String toString() {
    return Strings.toString("delegate", delegate, "cache", cache);
  }
  
  // --------------------------------------------------------------------------
  // Inner classes

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
      return substituteForCachedValue(next);
    }

    @Override
    public void remove() {
    }

  }

}
