package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;

/**
 * A thread-safe FIFO data structure that implements circular logic, removing the first object
 * in its internal list upon addition of new objects and after a given max capacity has been reached.
 * 
 * @author yduchesne
 *
 */
public class CircularBuffer<T> implements Iterable<T> {

  private int           capacity;
  private LinkedList<T> delegate;
  
  public CircularBuffer(int capacity) {
    this.capacity = capacity;
    this.delegate = new LinkedList<T>();
  }
 
  /**
   * @param item an {@link Object} to add to this instance.
   */
  public synchronized void add(T item) {
    delegate.add(item);
    if (delegate.size() > capacity) {
      delegate.removeFirst();
    }
  }
 
  /**
   * @param criteria a {@link Condition} to respect.
   * @return a {@link List} holding the objects that correspond to a subset of
   * the objects that this instance has - provided they match the given condition.
   */
  public synchronized List<T> subList(Condition<T> criteria) {
    return Collects.filterAsList(delegate, criteria);
  }
  
  /**
   * @return a new {@link List} holding this instance's current objects. 
   */
  public synchronized List<T> asList() {
    return new ArrayList<T>(delegate);
  }
  
  /**
   * Removes form this instance all objects that match a certain {@link Condition}, and returns them in
   * a new {@link List}.
   * 
   * @param criteria the {@link Condition} to check for.
   */
  public synchronized List<T> clear(Condition<T> criteria) {
    LinkedList<T> newList = new LinkedList<T>();
    List<T> toReturn = new ArrayList<T>();
    for (T e : delegate) {
      if (!criteria.apply(e)) {
        newList.add(e);
      } else {
        toReturn.add(e);
      }
    }
    delegate = newList;
    return toReturn;
  }
  
  /**
   * Removes all items from this instance.
   */
  public synchronized void clear() {
    delegate.clear();
  }
  
  /**
   * @return this instance's size.
   */
  public synchronized int size() {
    return delegate.size();
  }
  
  /**
   * Returns a thread-safe iterator over the objects that this instance currently contains.
   * 
   * @return a new {@link Iterator} over this instance's current state.
   */
  @Override
  public synchronized Iterator<T> iterator() {
    return new ArrayList<T>(delegate).iterator();
  }
  
  @Override
  public synchronized String toString() {
    return delegate.toString();
  }

}
