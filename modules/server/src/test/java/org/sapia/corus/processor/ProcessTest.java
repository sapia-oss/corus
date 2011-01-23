package org.sapia.corus.processor;

import junit.framework.TestCase;

import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.port.TestPortManager;


/**
 * @author Yanick Duchesne
 * 2002-03-03
 */
public class ProcessTest extends TestCase {
  /**
   * Constructor for VmProcessTest.
   * @param arg0
   */
  public ProcessTest(String arg0) {
    super(arg0);
  }

  public void testAddActivePort() throws Exception{
    Process p = new Process(null);
    for(int i = 0; i < 5; i++){
      p.addActivePort(new ActivePort("test" + i, i));
    }
    super.assertEquals(5, p.getActivePorts().size());
    p.releasePorts(new TestPortManager());
    super.assertEquals(0, p.getActivePorts().size());
  }

}
