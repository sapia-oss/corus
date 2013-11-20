package org.sapia.corus.client.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link List} implementation that will start flushing the last element in
 * its list upon adding new elements, if the maximum capacity is reached.
 * 
 * @author jdesrochers
 * 
 * @param <E>
 */
public class AutoFlushedBoundedList<E> extends ArrayList<E> {

  static final long serialVersionUID = 1L;

  private int capacity;

  /**
   * Creates a new {@link AutoFlushedBoundedList} instance.
   * 
   * @param aCapacity
   */
  public AutoFlushedBoundedList(int aCapacity) {
    super(aCapacity);
    capacity = aCapacity;
  }

  @Override
  public boolean add(E anElement) {
    ensureSpaceAvailable();

    super.add(0, anElement);
    return true;
  }

  public void add(int anIndex, E anElement) {
    ensureSpaceAvailable();

    super.add(anIndex, anElement);
  }

  private void ensureSpaceAvailable() {
    if (super.size() == capacity) {
      super.remove(capacity - 1);
    }
  }

}
