package org.sapia.corus.client.services.database.persistence;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.exceptions.db.StaleObjectException;
import org.sapia.corus.client.services.database.DbMap;

/**
 * An instance of this class corresponds to the persisted fields of an object.
 * 
 * @author yduchesne
 * 
 * @param <V>
 */
public class Record<V> implements Externalizable {

  public static final Long MIN_VERSION  = 1L;
  public static final Long NULL_VERSION = 0L;

  static final long serialVersionUID = 1L;

  Object[] values;

  public Record() {
  }

  Record(Object[] values) {
    this.values = values;
  }

  /**
   * @param index
   *          the index of the value to return.
   * @return the {@link Object} at the given index, or <code>null</code> if
   *         there is no object at that index.
   */
  public Object getValueAt(int index) throws ArrayIndexOutOfBoundsException {
    return values[index];
  }

  /**
   * Updates this record with the given instance.
   * 
   * @param cd
   *          the {@link ClassDescriptor} to which this instance's corresponding
   *          {@link FieldDescriptor} belongs.
   * @param instance
   *          the Object from which to copy field values.
   */
  public void updateWith(ClassDescriptor<V> cd, Object instance) {
    for (int i = 0; i < values.length; i++) {
      FieldDescriptor fd = cd.getFieldForIndex(i);
      Object toAssign = fd.invokeAccessor(instance);
      if (fd.isVersion()) {
        if (!values[i].equals(toAssign)) {
          throw new StaleObjectException(String.format("Stale object: %s; concurrent update has occurred", instance));
        } else {
          values[i] = incrementVersion((Long) values[i]);
          fd.invokeMutator(instance, values[i]);
        }
      } else {
        values[i] = toAssign;
      }
    }
  }

  /**
   * @param cd
   *          the {@link ClassDescriptor} to which this instance's corresponding
   *          {@link FieldDescriptor} belongs.
   * @param instance
   *          the Object from which to copy field values.
   * @return the newly created {@link Record}.
   */
  public static <T> Record<T> createFor(ClassDescriptor<T> cd, Object instance) {
    Object[] values = new Object[cd.getFieldCount()];
    for (int i = 0; i < values.length; i++) {
      FieldDescriptor fd = cd.getFieldForIndex(i);
      Object value = fd.invokeAccessor(instance);
      if (fd.isVersion()) {
        if (value == null || value.equals(NULL_VERSION)) {
          value = MIN_VERSION;
          fd.invokeMutator(instance, value);
        }
      }
      values[i] = value;
    }
    return new Record<T>(values);
  }
  
  /**
   * @param values one or more values that the returned record should wrap.
   * @return a {@link Record} wrapping the given values.
   */
  public static <T> Record<T> createFor(Object...values) {
    return new Record<T>(values);
  }

  @SuppressWarnings(value = { "unchecked", "rawtypes" })
  public static <T> Record<T> createFor(DbMap<?, T> db, Object instance) {
    Record<T> record = createFor(db.getClassDescriptor(), instance);
    if (instance instanceof Persistent) {
      ((Persistent) instance).setDbMap(db);
    }
    return record;
  }

  @SuppressWarnings(value = { "unchecked", "rawtypes" })
  public <K> V toObject(DbMap<K, V> db) {
    V toReturn = toObject(db.getClassDescriptor());
    if (toReturn instanceof Persistent) {
      ((Persistent) toReturn).setDbMap(db);
    }
    return toReturn;
  }

  @SuppressWarnings(value = { "unchecked", "rawtypes" })
  public <K> void populate(DbMap<K, V> db, V value) {
    populate(db.getClassDescriptor(), value);
    if (value instanceof Persistent) {
      ((Persistent) value).setDbMap(db);
    }
  }

  /**
   * @param cd
   *          the {@link ClassDescriptor} to which this instance's corresponding
   *          {@link FieldDescriptor} belongs.
   * @return the actual object whose state is kept by this instance.
   */
  public <K> V toObject(ClassDescriptor<V> cd) {
    V toReturn = cd.newInstance();
    for (int i = 0; i < values.length; i++) {
      FieldDescriptor fd = cd.getFieldForIndex(i);
      fd.invokeMutator(toReturn, values[i]);
    }
    return toReturn;
  }

  public <K> void populate(ClassDescriptor<V> cd, V value) {
    for (int i = 0; i < values.length; i++) {
      FieldDescriptor fd = cd.getFieldForIndex(i);
      fd.invokeMutator(value, values[i]);
    }
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.values = (Object[]) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(values);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      sb.append(i + ":" + values[i]);
      if (i < values.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  private Long incrementVersion(Long current) {
    if (current.longValue() + 1 == Long.MAX_VALUE) {
      return MIN_VERSION;
    } else {
      return new Long(current.longValue() + 1);
    }
  }

}
