package org.sapia.corus.processor;

import org.sapia.corus.taskmanager.core.DefaultThrottleKey;
import org.sapia.corus.taskmanager.core.ThrottleKey;

public class ProcessorThrottleKeys {

  /**
   * The {@link ThrottleKey} corresponding to process execution throttling.
   */
  public static final ThrottleKey PROCESS_EXEC = new DefaultThrottleKey("PROCESS_EXEC");

}
