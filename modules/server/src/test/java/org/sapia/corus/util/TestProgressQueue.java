package org.sapia.corus.util;

import org.sapia.corus.client.common.ProgressQueueImpl;


public class TestProgressQueue extends ProgressQueueImpl{
  
  
  @Override
  public synchronized void debug(Object msg) {
    super.debug(msg);
    System.out.println(">>" +msg);
  }
  
  @Override
  public synchronized void info(Object msg) {
    super.info(msg);
    System.out.println(">>" +msg);
  }
  
  @Override
  public synchronized void warning(Object msg) {
    super.warning(msg);
    System.out.println(">>" +msg);
  }
  
  @Override
  public synchronized void error(Object msg) {
    super.error(msg);
    System.out.println(">>" +msg);
  }
  
  

}
