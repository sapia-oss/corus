package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic, thread-safe queue implementation.
 * 
 * @author yduchesne
 *
 */
public class Queue<T> {
  
  private List<T> items = new ArrayList<T>();
  
  /**
   * @param item an item to add to this instance.
   */
  public synchronized void add(T item) {
    items.add(item);
  }
  
  /**
   * Returns this instance's elements in a new {@link List} and clears its
   * internal list.
   * 
   * @return this instance's elements.
   */
  public synchronized List<T> removeAll() {
    List<T> toReturn = new ArrayList<T>(items);
    items.clear();
    return toReturn;
  }
  
  /**
   * @return the first item in this queue.
   */
  public synchronized T removeFirst() {
    if (items.isEmpty()) {
      throw new IllegalStateException("Queue is empty");
    }
    return items.remove(0);
  }
  
  /**
   * @return the last item in this queue.
   */
  public synchronized T removeLast() {
    if (items.isEmpty()) {
      throw new IllegalStateException("Queue is empty");
    }
    return items.remove(items.size() - 1);
  }
  
  /**
   * The number of objects that this instance currently holds.
   */
  public synchronized void size() {
    items.size();
  }

}
