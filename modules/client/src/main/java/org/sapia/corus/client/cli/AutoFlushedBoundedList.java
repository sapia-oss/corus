package org.sapia.corus.client.cli;

import java.util.ArrayList;

public class AutoFlushedBoundedList<E> extends ArrayList<E> {

  private int _capacity;
  
  /**
   * Creates a new {@link AutoFlushedBoundedList} instance.
   *
   * @param aCapacity
   */
  public AutoFlushedBoundedList(int aCapacity) {
    super(aCapacity);
    _capacity = aCapacity;
  }
  
  /* (non-Javadoc)
   * @see java.util.concurrent.ArrayBlockingQueue#add(java.lang.Object)
   */
  @Override
  public boolean add(E anElement) {
    ensureSpaceAvailable();
    
    super.add(0, anElement);
    return true;
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#add(int, java.lang.Object)
   */
  @Override
  public void add(int anIndex, E anElement) {
    ensureSpaceAvailable();
    
    super.add(anIndex, anElement);
  }

  private void ensureSpaceAvailable() {
    if (super.size() == _capacity) {
      super.remove(_capacity-1);
    }
  }
  
}
