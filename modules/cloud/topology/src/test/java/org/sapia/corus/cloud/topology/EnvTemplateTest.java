package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EnvTemplateTest {

  private EnvTemplate template;

  @Before
  public void setUp() {
    template = new EnvTemplate();
    template.setName("template");
    
    Cluster c = new Cluster();
    c.setName("cluster");
    template.addCluster(c);
    
    Region r = new Region();
    r.setName("region");
    template.addRegion(r);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAddRegion_duplicate() {
    Region r = new Region();
    r.setName("region");
    template.addRegion(r);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddCluster_duplicate() {
    Cluster c = new Cluster();
    c.setName("cluster");
    template.addCluster(c);
  }

  @Test
  public void testCopyFrom() {
    EnvTemplate other = new EnvTemplate();
    
    Cluster c = new Cluster();
    c.setName("cluster1");
    other.addCluster(c);
    
    Region r = new Region();
    r.setName("region1");
    other.addRegion(r);
    
    template.copyFrom(other);
    
    assertEquals(2, template.getClusters().size());
    assertEquals(2, template.getRegions().size());
  }
  
  @Test
  public void testEquals() {
    EnvTemplate other = new EnvTemplate();
    other.setName("template");
    assertEquals(template, other);
  }

  @Test
  public void testEquals_false() {
    EnvTemplate other = new EnvTemplate();
    other.setName("other");
    assertNotEquals(template, other);
  }
}
