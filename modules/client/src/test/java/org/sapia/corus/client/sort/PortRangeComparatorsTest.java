package org.sapia.corus.client.sort;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.corus.client.services.port.PortRange;

public class PortRangeComparatorsTest {
  
  @Test
  public void testForName() throws Exception {
    PortRange r1 = range("c1");
    PortRange r2 = range("c2");
    
    assertTrue(PortRangeComparators.forName().compare(r1, r2) < 0);
    assertTrue(PortRangeComparators.forName().compare(r2, r1) > 0);
    assertTrue(PortRangeComparators.forName().compare(r1, r1) == 0);
  }

  
  private PortRange range(String name) throws Exception{
    PortRange r = new PortRange(name, 1, 2);
    return r;
  }

}
