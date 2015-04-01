package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ClusterTemplateTest {
 
  private ClusterTemplate template;
  
  @Before
  public void setUp() {
    template = new ClusterTemplate();
    template.setName("test");
    template.setInstances(10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddMachine_duplicates() {
    Machine m = new Machine();
    m.setImageId("testImage");
    m.setMaxInstances(1);
    m.setMinInstances(1);
    m.setName("testMachine");
    template.addMachine(m);
    template.addMachine(m);
  }

  @Test
  public void testCopyFrom_instances_set() {
    Machine m = new Machine();
    m.setImageId("testImage");
    m.setMaxInstances(1);
    m.setMinInstances(1);
    m.setName("testMachine");
    
    ClusterTemplate other = new ClusterTemplate();
    other.setInstances(1);
    other.addMachine(m);
    
    template.copyFrom(other);
    
    assertEquals(10, template.getInstances());
    assertEquals(1, template.getMachines().size());
  }
  
  @Test 
  public void testEquals() {
    ClusterTemplate other = new ClusterTemplate();
    other.setName("test");
    
    assertEquals(template, other);
  }

  @Test 
  public void testEquals_false() {
    ClusterTemplate other = new ClusterTemplate();
    other.setName("other");
    
    assertNotEquals(template, other);
  }
}
