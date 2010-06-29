package org.sapia.corus.client.services.db.persistence;

import java.io.Serializable;

import org.sapia.corus.client.services.db.DbMap;

/**
 * An instance of this class corresponds to the persisted fields of an object.
 * 
 * @author yduchesne
 *
 * @param <V>
 */
public class Record<V> implements Serializable{

  static final long serialVersionUID = 1L;
  
  Object[] values;
  
  public Record(){}
  
  
  Record(Object[] values){
    this.values = values;
  }
  
  /**
   * @param index the index of the value to return.
   * @return the {@link Object} at the given index, or <code>null</code> if there is
   * no object at that index.
   */
  public Object getValueAt(int index) throws ArrayIndexOutOfBoundsException{
    return values[index];
  }
  
  /**
   * Updates this record with the given instance.
   * 
   * @param cd the {@link ClassDescriptor} to which this instance's corresponding
   * {@link FieldDescriptor} belongs.
   * @param instance the Object from which to copy field values.
   */
  public void updateWith(ClassDescriptor<V> cd, Object instance){
    for(int i = 0; i < values.length; i++){
      FieldDescriptor fd = cd.getFieldForIndex(i);
      values[i]  = fd.invokeAccessor(instance);
    }
  }
  
  /**
   * @param cd the {@link ClassDescriptor} to which this instance's corresponding
   * {@link FieldDescriptor} belongs.
   * @param instance the Object from which to copy field values.
   * @return the newly created {@link Record}.
   */
  public static <T>  Record<T> createFor(ClassDescriptor<T> cd, Object instance){
    Object[] values = new Object[cd.getFieldCount()];
    for(int i = 0; i < values.length; i++){
      FieldDescriptor fd = cd.getFieldForIndex(i);
      values[i]  = fd.invokeAccessor(instance);
    }
    return new Record<T>(values);
  }
  
  @SuppressWarnings(value="unchecked")
  public static <T>  Record<T> createFor(DbMap<?, T> db, Object instance){
    Record<T> record = createFor(db.getClassDescriptor(), instance);
    if(instance instanceof Persistent){
      ((Persistent)instance).setDbMap(db);
    }
    return record;
  }
  
  @SuppressWarnings(value="unchecked")
  public <K> V toObject(DbMap<K, V> db){
    V toReturn = toObject(db.getClassDescriptor());
    if(toReturn instanceof Persistent){
      ((Persistent)toReturn).setDbMap(db);
    }
    return toReturn; 
  }

  @SuppressWarnings(value="unchecked")
  public <K> void populate(DbMap<K, V> db, V value){
    populate(db.getClassDescriptor(), value);
    if(value instanceof Persistent){
      ((Persistent)value).setDbMap(db);
    }
  }

  /**
   * @param cd the {@link ClassDescriptor} to which this instance's corresponding
   * {@link FieldDescriptor} belongs.
   * @return the actual object whose state is kept by this instance.
   */
  public <K> V toObject(ClassDescriptor<V> cd){
    V toReturn = cd.newInstance();
    for(int i = 0; i < values.length; i++){
      FieldDescriptor fd = cd.getFieldForIndex(i);
      fd.invokeMutator(toReturn, values[i]);
    }
    return toReturn; 
  }  

  public <K> void populate(ClassDescriptor<V> cd, V value){
    for(int i = 0; i < values.length; i++){
      FieldDescriptor fd = cd.getFieldForIndex(i);
      fd.invokeMutator(value, values[i]);
    }
  }

  public String toString(){
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < values.length; i++){
      sb.append(i+":"+values[i]);
      if(i < values.length - 1){
        sb.append(",");
      }
    }
    return sb.toString();
  }
  
}
