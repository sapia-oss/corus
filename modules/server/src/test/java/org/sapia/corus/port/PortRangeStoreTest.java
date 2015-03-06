package org.sapia.corus.port;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.database.HashDbMap;

public class PortRangeStoreTest {
  
  PortRangeStore store;

  @Before
  public void setUp() throws Exception {
    store = new PortRangeStore(new HashDbMap<String, PortRange>(new ClassDescriptor<PortRange>(PortRange.class)));
  }

  @Test
  public void testWritePortRanges() throws Exception{
    PortRange r1 = new PortRange("test", 10, 20);
    PortRange r2 = new PortRange("test", 15, 25);
    store.writeRange(r1);
    store.writeRange(r2);
  }


  @Test
  public void testContainsRange() throws Exception{
    assertTrue(!store.containsRange("test"));
    PortRange r = new PortRange("test", 10, 20);
    store.writeRange(r);
    assertTrue(store.containsRange("test"));
  }

  @Test
  public void testReadRange() throws Exception{
    PortRange r = new PortRange("test", 10, 20);
    store.writeRange(r);
    r = store.readRange("test");
    assertTrue("No port range found", r != null);
  }
  
  @Test
  public void testReadWriteRangeSinglePort() throws Exception{
    PortRange r = new PortRange("test", 10, 10);
    store.writeRange(r);
    r = store.readRange("test");
    assertTrue("No port range found", r != null);
  }
}
