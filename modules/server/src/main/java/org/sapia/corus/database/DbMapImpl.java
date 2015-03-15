package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RecordMatcher;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.corus.client.services.database.persistence.Template;
import org.sapia.corus.client.services.database.persistence.TemplateMatcher;
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

  /**
   * Constructs a new instance of this class.
   */
  DbMapImpl(Class<K> keyType, Class<V> valueType, TxFacade db, ConcurrentNavigableMap<K, Record<V>> map, Archiver<K, V> archiver) {
    this.db         = db;
    this.map        = map;
    this.archiver   = archiver;
    classDescriptor = new ClassDescriptor<V>(valueType);
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
