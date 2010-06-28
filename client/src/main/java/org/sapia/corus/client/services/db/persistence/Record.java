package org.sapia.corus.client.services.db.persistence;

import java.io.Serializable;

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
  
  /**
   * @param cd the {@link ClassDescriptor} to which this instance's corresponding
   * {@link FieldDescriptor} belongs.
   * @return the actual object whose state is kept by this instance.
   */
  public V toObject(ClassDescriptor<V> cd){
    V toReturn = cd.newInstance();
    for(int i = 0; i < values.length; i++){
      FieldDescriptor fd = cd.getFieldForIndex(i);
      fd.invokeMutator(toReturn, values[i]);
    }
    return toReturn; 
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
