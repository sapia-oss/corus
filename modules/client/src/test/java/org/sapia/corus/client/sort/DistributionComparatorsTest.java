package org.sapia.corus.client.sort;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public class DistributionComparatorsTest {
  
  @Test
  public void testForName() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n2", "v1");
    
    assertTrue(DistributionComparators.forName().compare(d1, d2) < 0);
    assertTrue(DistributionComparators.forName().compare(d2, d1) > 0);
    assertTrue(DistributionComparators.forName().compare(d1, d1) == 0);
  }

  @Test
  public void testForVersion() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n1", "v2");
    
    assertTrue(DistributionComparators.forVersion().compare(d1, d2) < 0);
    assertTrue(DistributionComparators.forVersion().compare(d2, d1) > 0);
    assertTrue(DistributionComparators.forVersion().compare(d1, d1) == 0);
  }
  
  private Distribution distribution(String name, String version) {
    return new Distribution(name, version);
  }

}
