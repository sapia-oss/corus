package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RegionTest {

  private RegionTemplate template;
  private Region region;
  
  @Before
  public void setUp() {
    template = new RegionTemplate();
    template.setName("template");
    template.addZone(Zone.of("zone"));
    
    region = new Region();
    region.setTemplateRef("template");
    region.setName("region");
  }
  
  @Test
  public void testRender() {
    region.render(TopologyContext.newInstance().addRegionTemplate(template));
    assertEquals(1, region.getZones().size());
  }

}
