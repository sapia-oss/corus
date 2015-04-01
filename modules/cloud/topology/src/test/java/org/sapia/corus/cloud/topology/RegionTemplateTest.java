package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RegionTemplateTest {

  private RegionTemplate template;
  
  @Before
  public void setUp() {
    template = new RegionTemplate();
    template.setName("template");
    template.addZone(Zone.of("zone"));
  }
  
  @Test
  public void testCopyFrom() {
    RegionTemplate other = new RegionTemplate();
    other.setName("other");
    other.addZone(Zone.of("zone2"));
    
    template.copyFrom(other);
    
    assertEquals(2, template.getZones().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddZone_duplicate() {
    Zone dup = new Zone();
    dup.setName("zone");
    template.addZone(dup);
  }
  
  @Test
  public void testEqualsObject() {
    RegionTemplate p1 = new RegionTemplate();
    p1.setName("p1");

    RegionTemplate p2 = new RegionTemplate();
    p2.setName("p2");

    RegionTemplate p3 = new RegionTemplate();
    p3.setName("p1");
    
    assertNotEquals(p1, p2);
    assertEquals(p1, p3);
  }
}
