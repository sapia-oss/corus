package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MachineTemplateTest {
  
  private MachineTemplate template;
  private MachineTemplate other;
  
  @Before
  public void setUp() {
    template = new MachineTemplate();
    template.setImageId("testImage");
    template.setMinInstances(1);
    template.setMaxInstances(10);
    template.setName("testTemplate");
    template.setServerTags("tag1");
    template.createServerProperties().addProperty("svrProp1", "p1");
    template.createProcessProperties().addProperty("procProp1", "p1");
   
    other = new MachineTemplate();
    other.setMinInstances(5);
    other.setMaxInstances(15);
    other.setImageId("testImage2");
    PropertyCollection serverProps  = new PropertyCollection();
    serverProps.addProperty("svrProp2", "p2");
    
    PropertyCollection processProps = new PropertyCollection();
    processProps.addProperty("procProp2", "p2");
    
    other.setServerProperties(serverProps);
    other.setProcessProperties(processProps);
    other.setServerTags("tag2,tag3");
  }

  
  @Test
  public void testCopyFrom() {
    template.copyFrom(other);
    assertTrue(template.createServerProperties().getProperties().contains(CorusProperty.of("svrProp1", "p1")));
    assertTrue(template.createProcessProperties().getProperties().contains(CorusProperty.of("procProp1", "p1")));
    assertTrue(template.createServerProperties().getProperties().contains(CorusProperty.of("svrProp2", "p2")));
    assertTrue(template.createProcessProperties().getProperties().contains(CorusProperty.of("procProp2", "p2")));
   
    assertTrue(template.getServerTags().contains(ServerTag.of("tag1")));
    assertTrue(template.getServerTags().contains(ServerTag.of("tag2")));
    assertTrue(template.getServerTags().contains(ServerTag.of("tag3")));
    
    assertEquals(1, template.getMinInstances());
    assertEquals(10, template.getMaxInstances());
  }
  
  @Test
  public void testCopyFrom_properties_priority() {
    template.createServerProperties().addProperty("svrProp2", "p1");
    template.createProcessProperties().addProperty("procProp2", "p1");
    
    template.copyFrom(other);
    
    assertTrue(template.createServerProperties().getProperties().contains(CorusProperty.of("svrProp1", "p1")));
    assertTrue(template.createProcessProperties().getProperties().contains(CorusProperty.of("procProp1", "p1")));
    assertTrue(template.createServerProperties().getProperties().contains(CorusProperty.of("svrProp2", "p1")));
    assertTrue(template.createProcessProperties().getProperties().contains(CorusProperty.of("procProp2", "p1")));
  }
  
  @Test
  public void testCopyFrom_minInstances_priority() {
    template.setMinInstances(-1);
    
    template.copyFrom(other);
    
    assertEquals(5, template.getMinInstances());
  }
  
  @Test
  public void testCopyFrom_maxInstances_priority() {
    template.setMaxInstances(-1);
    
    template.copyFrom(other);
    
    assertEquals(15, template.getMaxInstances());
  }
  
  @Test
  public void testCopyFrom_imageId() {
    template.setImageId(null);
    
    template.copyFrom(other);
    
    assertEquals("testImage2", template.getImageId());
  }
  
  @Test
  public void testEquals() {
    MachineTemplate other = new MachineTemplate();
    other.setName("testTemplate");
    
    assertEquals(template, other);
  }

  @Test
  public void testEquals_false() {
    MachineTemplate other = new MachineTemplate();
    other.setName("other");
    
    assertNotEquals(template, other);
  }
}
