package org.sapia.corus.client.services.repository;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class RepoDistributionTest {

  RepoDistribution dist1, dist2;
  
  @Before
  public void setUp() {
    dist1 = new RepoDistribution("dist1", "1.0");
    dist2 = new RepoDistribution("dist2", "1.0");
  }
  
  @Test
  public void testSerialization() throws Exception {
    byte[] content = Serialization.serialize(dist1);
    assertEquals(dist1, Serialization.deserialize(content));
  }

  @Test
  public void testEquals() {
    assertEquals(dist1, dist1);
  }
  
  @Test
  public void testNotEquals() {
    assertNotSame(dist1, dist2);
  }

}
