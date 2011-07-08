package org.sapia.corus.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.sapia.corus.client.exceptions.core.IORuntimeException;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.Record;
import org.sapia.corus.db.persistence.Template;
import org.sapia.corus.db.persistence.TemplateMatcher;

import jdbm.JDBMEnumeration;
import jdbm.JDBMHashtable;


/**
 * A {@link DbMap} implementation on top of JDMB.
 * 
 * @author Yanick Duchesne
 */
public class DbMapImpl<K, V> implements DbMap<K, V> {
  
  private JDBMHashtable _hashtable;
  private ClassDescriptor<V> _classDescriptor;

  DbMapImpl(Class<K> keyType, Class<V> valueType, JDBMHashtable hashtable) {
    _hashtable = hashtable;
    _classDescriptor = new ClassDescriptor<V>(valueType);
  }
  
  @Override
  public ClassDescriptor<V> getClassDescriptor() {
    return _classDescriptor;
  }

  @Override
  public void close() {
    try {
      _hashtable.dispose();
    } catch (IOException e) {
      // noop;
    }
  }

  @SuppressWarnings(value="unchecked")
  @Override
  public V get(K key) {
    try {
      Record<V> rec = (Record<V>)_hashtable.get(key);
      if(rec == null){
        return null;
      }
      else{
        V obj = rec.toObject(this);
        return obj;
      }
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }
  
  @SuppressWarnings(value="unchecked")
  public void refresh(K key, V value) {
    try {
      Record<V> rec = (Record<V>)_hashtable.get(key);
      if(rec == null){
        throw new IllegalArgumentException(String.format("No record found for %s", key));
      }
      else{
        rec.populate(this, value);
      }
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public Iterator<K> keys() {
    try {
      return new KeyIterator(_hashtable.keys());
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public void put(K key, V value) {
    try {
      Record<V> r = Record.createFor(this, value);
      _hashtable.put(key, r);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public void remove(K key) {
    try {
      _hashtable.remove(key);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }
  
  @Override
  public Iterator<V> values() {
    try {
      return new RecordIterator(this, _hashtable.values());
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public org.sapia.corus.client.services.db.RecordMatcher<V> createMatcherFor(V template) {
    return new TemplateMatcher<V>(new Template<V>(_classDescriptor, template));
  }
  
  @Override
  @SuppressWarnings(value="unchecked")
  public Collection<V> values(RecordMatcher<V> matcher) {
    try {
      Collection<V> result = new ArrayList<V>();
      JDBMEnumeration enumeration = _hashtable.values();
      while(enumeration.hasMoreElements()){
        Record<V> rec = (Record<V>)enumeration.nextElement();
        if(matcher.matches(rec)){
          result.add(rec.toObject(this));
        }
      }
      return result;
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  @Override
  public void clear() {
    
    Iterator<K> keys = keys();    
    while(keys.hasNext()){
      K key = keys.next();
      remove(key);
    }

    // weird bug: some keys may be remaining, so 2nd pass.
    keys = keys();
    while(keys.hasNext()){
      K key = keys.next();
      remove(key);
    }
  }
  
  /*//////////////////////////////////////////////////
                    INNER CLASSES
  //////////////////////////////////////////////////*/

  
  class RecordIterator implements Iterator<V> {
    private JDBMEnumeration _enum;
    private DbMapImpl<K, V> _parent;
    
    RecordIterator(DbMapImpl<K, V> parent, JDBMEnumeration anEnum) {
      _parent = parent;
      _enum = anEnum;
    }

    public boolean hasNext() {
      try {
        return _enum.hasMoreElements();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    @SuppressWarnings(value="unchecked")
    public V next() {
      try {
        Record<V> record = (Record<V>)_enum.nextElement();
        return (V)record.toObject(_parent);
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }
  
  class KeyIterator implements Iterator<K> {
    private JDBMEnumeration _enum;

    KeyIterator(JDBMEnumeration anEnum) {
      _enum = anEnum;
    }

    public boolean hasNext() {
      try {
        return _enum.hasMoreElements();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    @SuppressWarnings(value="unchecked")
    public K next() {
      try {
        return (K)_enum.nextElement();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }
}
