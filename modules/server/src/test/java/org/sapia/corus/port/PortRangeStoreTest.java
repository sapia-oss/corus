package org.sapia.corus.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.database.InMemoryArchiver;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

public class PortRangeStoreTest {
  
  PortRangeStore store;

  @Before
  public void setUp() throws Exception {
    store = new PortRangeStore(new InMemoryDbMap<String, PortRange>(new ClassDescriptor<PortRange>(PortRange.class), new InMemoryArchiver<String, PortRange>(), new Func<PortRange, JsonInput>() {
      public PortRange call(JsonInput arg0) {
        return PortRange.fromJson(arg0);
      }
    }));
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
  
  @Test
  public void testArchive() throws Exception {
    PortRange r1 = new PortRange("test1", 10, 20);
    PortRange r2 = new PortRange("test2", 15, 25);
    store.writeRange(r1);
    store.writeRange(r2);
    
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();
    
    store.unarchiveRanges(RevId.valueOf("rev"));
    
    assertEquals(2, Collects.convertAsList(store.getPortRanges(), new Func<PortRange, PortRange>() {
      @Override
      public PortRange call(PortRange arg) {
        return arg;
      }
    }).size());
  }
  
  @Test
  public void testArchive_clear_previous_rev() throws Exception {
    PortRange r1 = new PortRange("test1", 10, 20);
    store.writeRange(r1);
    
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();

    PortRange r2 = new PortRange("test2", 10, 20);
    store.writeRange(r2);
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();
    store.unarchiveRanges(RevId.valueOf("rev"));
    
    List<PortRange> ports = Collects.convertAsList(store.getPortRanges(), new Func<PortRange, PortRange>() {
      @Override
      public PortRange call(PortRange arg) {
        return arg;
      }
    });
    assertEquals(1, ports.size());
    assertEquals("test2", ports.get(0).getName());
  }
}
