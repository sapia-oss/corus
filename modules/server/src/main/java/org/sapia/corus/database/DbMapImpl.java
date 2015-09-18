package org.sapia.corus.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

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
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * A {@link DbMap} implementation on top of MapDB.
 * 
 * @author Yanick Duchesne
 */
public class DbMapImpl<K, V> implements DbMap<K, V> {
  
  /**
   * Abstracts transactional demarcation.
   * 
   * @author yduchesne
   *
   */
  public interface TxFacade {
    
    /**
     * Commits the last operation.
     */
    public void commit();
    
  }
  
  // --------------------------------------------------------------------------

  private TxFacade                             db;
  private ConcurrentNavigableMap<K, Record<V>> map;
  private Archiver<K, V>                       archiver;
  private ClassDescriptor<V>                   classDescriptor;
  private Func<V, JsonInput>                   fromJsonFunc;

  DbMapImpl(
      Class<K>           keyType, 
      Class<V>           valueType, 
      TxFacade           db, 
      ConcurrentNavigableMap<K, Record<V>> map, 
      Archiver<K, V>     archiver,
      Func<V, JsonInput> fromJsonFunc) {
    this.db              = db;
    this.map             = map;
    this.archiver        = archiver;
    this.fromJsonFunc    = fromJsonFunc;
    this.classDescriptor = new ClassDescriptor<V>(valueType);
  }
  
  DbMapImpl(
      Class<K>       keyType, 
      Class<V>       valueType, 
      TxFacade       db, 
      ConcurrentNavigableMap<K, Record<V>> map, 
      Archiver<K, V> archiver) {
    this(keyType, valueType, db, map, archiver, fromJsonFunc(valueType));
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
    Record<V> rec = map.get(key);
    if (rec == null) {
      return null;
    } else {
      V obj = rec.toObject(this);
      return obj;
    }
  }

  public void refresh(K key, V value) {
    Record<V> rec = map.get(key);
    if (rec == null) {
      throw new IllegalArgumentException(String.format("No record found for %s", key));
    } else {
      rec.populate(this, value);
    }
  }

  @Override
  public Iterator<K> keys() {
    return map.keySet().iterator();
  }

  @Override
  public void put(K key, V value) {
    Record<V> r = Record.createFor(this, value);
    map.put(key, r);
    db.commit();
  }

  @Override
  public void remove(K key) {
    map.remove(key);
    db.commit();
  }

  @Override
  public Iterator<V> values() {
    return new RecordIterator(map.values().iterator());
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
    for (Record<V> rec : map.values()) {
      if (matcher.matches(rec)) {
        result.add(rec.toObject(this));
      }
    }
    return result;
  }

  @Override
  public void clear() {
    map.clear();
    db.commit();
  }
  
  @Override
  public void archive(RevId revId, K key) {
    Record<V> rec = map.get(key);
    if (rec != null) {
      archiver.archive(revId, key, rec);
    } 
    db.commit();
  }
  
  @Override
  public void archive(RevId revId, Collection<K> keys) {
    for (K k : keys) {
      Record<V> rec = map.get(k);
      if (rec != null) {
        archiver.archive(revId, k, rec);
      } 
    }
    if (!keys.isEmpty()) {
      db.commit();
    }
  }
  
  @Override
  public List<K> unarchive(RevId revId) {
    List<PairTuple<K, Record<V>>> records = archiver.unarchive(revId, true);
    try {
      return Collects.convertAsList(records, new Func<K, PairTuple<K, Record<V>>>() {
        public K call(PairTuple<K, Record<V>> arg) {
          map.put(arg.getLeft(), arg.getRight());
          return arg.getLeft();
        }
      });
    } finally {
      db.commit();
    }
  }
  
  @Override
  public void clearArchive(RevId revId) {
    archiver.clear(revId);
    db.commit();
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

  private static <V> Func<V, JsonInput> fromJsonFunc(final Class<V> valueType) {
    try {
      final Method m  = valueType.getMethod("fromJson", new Class<?>[] { JsonInput.class });
      
      Assertions.illegalState(!Modifier.isStatic(m.getModifiers()), "Expected static fromJson() method for: " + valueType);
      Assertions.illegalState(!valueType.equals(m.getReturnType()), "Expected " + valueType.getName() + " for return type of method: " + m);
      
      return new Func<V, JsonInput>() {
        @Override
        public V call(JsonInput in) {
          try {
            return valueType.cast(m.invoke(null, new Object[] {in}));
          } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke method: " + m, e.getTargetException());
          } catch (Exception e) {
            throw new IllegalStateException("Could not invoke method: " + m, e);
          }
        }
      };
    } catch (Exception e) {
      throw new IllegalStateException("Error trying to introspect fromJson(JsonInput) method on class: " + valueType.getName());
    }
  }
  
  // --------------------------------------------------------------------------
  // inner classes
  
  class RecordIterator implements Iterator<V> {
    private Iterator<Record<V>> records;

    RecordIterator(Iterator<Record<V>> records) {
      this.records = records;
    }

    @Override
    public boolean hasNext() {
      return records.hasNext();
    }

    @Override
    public V next() {
      Record<V> record = records.next();
      return (V) record.toObject(DbMapImpl.this);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }
}
