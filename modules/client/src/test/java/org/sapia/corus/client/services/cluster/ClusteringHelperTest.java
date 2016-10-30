package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

public class ClusteringHelperTest {

  private Set<ServerAddress> visited;
  private Set<ServerAddress> siblings;
  private Set<ServerAddress> targets;
 
  @Before
  public void setUp() {
    visited = new HashSet<>();
    siblings = new HashSet<>();
    targets = new HashSet<>();
    
    for (int i = 0; i < 5; i++) {
      TCPAddress addr = new TCPAddress("test", "host", i); 
      siblings.add(addr);
      targets.add(addr);
    }

  }

  @Test
  public void testSelectNextTarget() {
    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings, targets);
    
    assertFalse("Address should not be null", addr == null);
    assertEquals(1, visited.size());
    assertTrue(visited.contains(addr));
  }

  @Test
  public void testSelectNextTargetAllVisited() {
    visited.addAll(siblings);

    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings, targets);
    assertTrue("Address should be null", addr == null);
    assertEquals(5, visited.size());
  }
  
  @Test
  public void testSelectNextTargetsEmpty() {
    targets.clear();
    
    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings, targets);

    assertFalse("Address should not be null", addr == null);
    assertEquals(1, visited.size());
    assertTrue(visited.contains(addr));
  }
  
  @Test
  public void testSelectNextTargetsSpecific() {
    siblings.clear();
    
    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings, targets);

    assertFalse("Address should not be null", addr == null);
    assertEquals(1, visited.size());
    assertTrue(visited.contains(addr));
  }

}