package org.sapia.corus.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.database.InMemoryArchiver;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

public class PortRangeStoreTest {
  
  PortRangeStore store;

  @Before
  public void setUp() throws Exception {
    store = new PortRangeStore(new InMemoryDbMap<String, PortRangeDefinition>(new ClassDescriptor<PortRangeDefinition>(PortRangeDefinition.class),
        new InMemoryArchiver<String, PortRangeDefinition>(), new Func<PortRangeDefinition, JsonInput>() {
      public PortRangeDefinition call(JsonInput arg0) {
        return PortRangeDefinition.fromJson(arg0);
      }
    }));
  }

  @Test
  public void testWritePortRanges() throws Exception{
    PortRangeDefinition r1 = new PortRangeDefinition("test", 10, 20);
    PortRangeDefinition r2 = new PortRangeDefinition("test", 15, 25);
    store.writeRange(r1);
    store.writeRange(r2);
  }

  @Test
  public void testContainsRange() throws Exception{
    assertTrue(!store.containsRange("test"));
    PortRangeDefinition r = new PortRangeDefinition("test", 10, 20);
    store.writeRange(r);
    assertTrue(store.containsRange("test"));
  }

  @Test
  public void testReadRange() throws Exception{
    PortRangeDefinition r = new PortRangeDefinition("test", 10, 20);
    store.writeRange(r);
    r = store.readRange("test");
    assertTrue("No port range found", r != null);
  }
  
  @Test
  public void testReadWriteRangeSinglePort() throws Exception{
    PortRangeDefinition r = new PortRangeDefinition("test", 10, 10);
    store.writeRange(r);
    r = store.readRange("test");
    assertTrue("No port range found", r != null);
  }
  
  @Test
  public void testArchive() throws Exception {
    PortRangeDefinition r1 = new PortRangeDefinition("test1", 10, 20);
    PortRangeDefinition r2 = new PortRangeDefinition("test2", 15, 25);
    store.writeRange(r1);
    store.writeRange(r2);
    
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();
    
    store.unarchiveRanges(RevId.valueOf("rev"));
    
    assertEquals(2, store.getPortRanges().size());
  }
  
  @Test
  public void testArchive_clear_previous_rev() throws Exception {
    PortRangeDefinition r1 = new PortRangeDefinition("test1", 10, 20);
    store.writeRange(r1);
    
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();

    PortRangeDefinition r2 = new PortRangeDefinition("test2", 10, 20);
    store.writeRange(r2);
    store.archiveRanges(RevId.valueOf("rev"));
    store.clear();
    store.unarchiveRanges(RevId.valueOf("rev"));
    
    List<PortRangeDefinition> ports = store.getPortRanges();
    assertEquals(1, ports.size());
    assertEquals("test2", ports.get(0).getName());
  }
}
