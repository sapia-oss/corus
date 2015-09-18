package org.sapia.corus.taskmanager.core;

import org.sapia.corus.client.common.CircularBuffer;
import org.sapia.corus.client.common.ProgressMsg;

/**
 * Buffers {@link ProgressMsg} instances.
 * 
 * @author yduchesne
 *
 */
public class ProgressBuffer extends CircularBuffer<ProgressMsg> {

  private int level;
  
  public ProgressBuffer(int capacity, int level) {
    super(capacity);
    this.level = level;
  }
  
  @Override
  public synchronized void add(ProgressMsg item) {
    if (item.getStatus() >= level) {
      super.add(item);
    }
  }

}
