package org.sapia.corus.client.rest;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;

/**
 * A support class for implementing REST resources.
 * 
 * @author yduchesne
 *
 */
public abstract class ResourceSupport {

  /**
   * @param queue a {@link ProgressQueue}.
   * @return the {@link ProgressResult} corresponding to the consumed {@link ProgressQueue}.
   */
  protected ProgressResult progress(ProgressQueue queue) {
    List<String> accumulator = new ArrayList<String>();
    if (queue == null) {
      return new ProgressResult(new ArrayList<String>());
    } 
    while (queue.hasNext()) {
      List<ProgressMsg> msgs = queue.next();

      for (int i = 0; i < msgs.size(); i++) {
        ProgressMsg msg = (ProgressMsg) msgs.get(i);
       
        if (msg.isError()) {
          if (msg.isThrowable()) {
            accumulator.add(msg.getThrowable().getMessage());
            return new ProgressResult(accumulator, msg.getThrowable());
          } else {
            accumulator.add(msg.getMessage().toString());
            return new ProgressResult(accumulator).error();
          }
        } else {
          if (msg.isThrowable()) {
            accumulator.add(msg.getThrowable().getMessage());
          } else {
            accumulator.add(msg.getMessage().toString());
          }
        }
      }
    }    
    return new ProgressResult(accumulator);
  }
}
