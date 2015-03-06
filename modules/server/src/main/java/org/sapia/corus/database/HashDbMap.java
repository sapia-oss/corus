package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.Record;
import org.sapia.corus.client.services.db.persistence.Template;
import org.sapia.corus.client.services.db.persistence.TemplateMatcher;

/**
 * A {@link DbMap} implementation around a {@link Map}.
 * 
 * @author Yanick Duchesne
 * 
 */
public class HashDbMap<K, V> implements DbMap<K, V> {
  private Map<K, Record<V>> map = new HashMap<K, Record<V>>();
  private ClassDescriptor<V> classDescriptor;

  public HashDbMap(ClassDescriptor<V> cd) {
    classDescriptor = cd;
  }

  @Override
  public ClassDescriptor<V> getClassDescriptor() {
    return classDescriptor;
  }

  public void close() {
  }

  public V get(K key) {
    Record<V> record = map.get(key);
    if (record != null) {
      return record.toObject(this);
    } else {
      return null;
    }
  }

  public void refresh(K key, V value) {
    Record<V> record = map.get(key);
    if (record == null) {
      throw new IllegalArgumentException(String.format("No record found for %s", key));
    } else {
      record.populate(this, value);
    }
  }

  public Iterator<K> keys() {
    return map.keySet().iterator();
  }

  public void put(K key, V value) {
    map.put(key, Record.createFor(this, value));
  }

  public void remove(K key) {
    map.remove(key);
  }

  public Iterator<V> values() {
    return new RecordIterator(this, map.values().iterator());
  }
  
  @Override
  public Iterator<V> iterator() {
    return values();
  }

  public org.sapia.corus.client.services.db.RecordMatcher<V> createMatcherFor(V template) {
    return new TemplateMatcher<V>(new Template<V>(classDescriptor, template));
  }

  public Collection<V> values(RecordMatcher<V> matcher) {
    Collection<V> result = new ArrayList<V>();
    Iterator<Record<V>> iterator = map.values().iterator();
    while (iterator.hasNext()) {
      Record<V> rec = iterator.next();
      if (matcher.matches(rec)) {
        V obj = rec.toObject(this);
        result.add(obj);
      }
    }
    return result;
  }

  public void clear() {
    map.clear();
  }
  
  @Override
  public String toString() {
    return map.toString();
  }
  
  // --------------------------------------------------------------------------
  // Inner classes

  class RecordIterator implements Iterator<V> {

    private HashDbMap<K, V> parent;
    private Iterator<Record<V>> delegate;

    public RecordIterator(HashDbMap<K, V> parent, Iterator<Record<V>> delegate) {
      this.parent = parent;
      this.delegate = delegate;
    }

    @Override
    public V next() {
      Record<V> rec = delegate.next();
      return rec.toObject(parent);
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public void remove() {
      delegate.remove();
    }
  }
}
