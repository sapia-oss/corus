package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RecordMatcher;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Persistent;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.corus.client.services.database.persistence.Template;
import org.sapia.corus.client.services.database.persistence.TemplateMatcher;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * A {@link DbMap} implementation around a {@link Map}.
 * 
 * @author Yanick Duchesne
 * 
 */
public class InMemoryDbMap<K, V> implements DbMap<K, V> {
  private Map<K, Record<V>>  map      = new HashMap<K, Record<V>>();
  private ClassDescriptor<V> classDescriptor;
  private Archiver<K, V>     archiver = new InMemoryArchiver<K, V>();
  private Func<V, JsonInput> fromJsonFunc;

  public InMemoryDbMap(ClassDescriptor<V> cd, Func<V, JsonInput> fromJsonFunc) {
    this(cd, new NullArchiver<K, V>(), fromJsonFunc);
  }
  
  public InMemoryDbMap(ClassDescriptor<V> cd, Archiver<K, V> archiver, Func<V, JsonInput> fromJsonFunc) {
    this.classDescriptor = cd;
    this.archiver        = archiver;
    this.fromJsonFunc    = fromJsonFunc;
  }

  @Override
  public ClassDescriptor<V> getClassDescriptor() {
    return classDescriptor;
  }

  @Override
  public void close() {
  }

  @Override
  public V get(K key) {
    Record<V> record = map.get(key);
    if (record != null) {
      return record.toObject(this);
    } else {
      return null;
    }
  }

  @Override
  public void refresh(K key, V value) {
    Record<V> record = map.get(key);
    if (record == null) {
      throw new IllegalArgumentException(String.format("No record found for %s", key));
    } else {
      record.populate(this, value);
    }
  }

  @Override
  public Iterator<K> keys() {
    return map.keySet().iterator();
  }

  @Override
  public void put(K key, V value) {
    map.put(key, Record.createFor(this, value));
  }

  @Override
  public void remove(K key) {
    map.remove(key);
  }

  @Override
  public Iterator<V> values() {
    return new RecordIterator(this, map.values().iterator());
  }
  
  @Override
  public Iterator<V> iterator() {
    return values();
  }

  @Override
  public RecordMatcher<V> createMatcherFor(V template) {
    return new TemplateMatcher<V>(new Template<V>(classDescriptor, template));
  }

  @Override
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

  @Override
  public void clear() {
    map.clear();
  }
  
  @Override
  public void archive(RevId revId, K key) {
    Record<V> rec = map.get(key);
    if (rec != null) {
      archiver.archive(revId, key, rec);
    } 
  }
  
  @Override
  public void archive(RevId revId, Collection<K> keys) {
    for (K k : keys) {
      Record<V> rec = map.get(k);
      if (rec != null) {
        archiver.archive(revId, k, rec);
      } 
    }
  }
  
  @Override
  public List<K> unarchive(RevId revId) {
    List<PairTuple<K, Record<V>>> records = archiver.unarchive(revId, true);
    return Collects.convertAsList(records, new Func<K, PairTuple<K, Record<V>>>() {
      public K call(PairTuple<K, Record<V>> arg) {
        map.put(arg.getLeft(), arg.getRight());
        return arg.getLeft();
      }
    });
  }
  
  @Override
  public void clearArchive(RevId revId) {
    archiver.clear(revId);
  }
  
  @Override
  public void dump(JsonStream stream) {
    stream.field(classDescriptor.getType().getName()).beginArray();
    Iterator<K> keys = keys();
    while (keys.hasNext()) {
      V value = get(keys.next());
      if (value instanceof JsonStreamable) {
        ((JsonStreamable) value).toJson(stream, ContentLevel.DETAIL);
      }
    }
    stream.endArray();
    
    archiver.dump(stream, classDescriptor);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void load(JsonInput input) {
    if (JsonStreamable.class.isAssignableFrom(classDescriptor.getType())) {
      for (JsonInput in : input.iterate(classDescriptor.getType().getName())) {
        V value = fromJsonFunc.call(in);
        map.put(((Persistent<K, V>) value).getKey(), Record.createFor(classDescriptor, value));
      }
      
      archiver.load(input, classDescriptor, fromJsonFunc);
    }
  }
  
  @Override
  public String toString() {
    return map.toString();
  }
  
  // --------------------------------------------------------------------------
  // Inner classes

  class RecordIterator implements Iterator<V> {

    private InMemoryDbMap<K, V> parent;
    private Iterator<Record<V>> delegate;

    public RecordIterator(InMemoryDbMap<K, V> parent, Iterator<Record<V>> delegate) {
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
