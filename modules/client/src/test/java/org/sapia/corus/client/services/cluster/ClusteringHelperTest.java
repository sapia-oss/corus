package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Collections2;

public class ClusteringHelperTest {

  @Test
  public void testSelectNextTarget() {

    Set<ServerAddress> visited = new HashSet<ServerAddress>();
    Set<ServerAddress> siblings = new HashSet<ServerAddress>();

    for (int i = 0; i < 5; i++) {
      siblings.add(new TCPAddress("test", "host", i));
    }

    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings);
    assertFalse("Address should not be null", addr == null);
    assertEquals(1, visited.size());
  }

  @Test
  public void testSelectNextTargetAllVisited() {

    Set<ServerAddress> visited = new HashSet<ServerAddress>();
    Set<ServerAddress> siblings = new HashSet<ServerAddress>();

    for (int i = 0; i < 5; i++) {
      siblings.add(new TCPAddress("test", "host", i));
    }

    visited.addAll(siblings);

    ServerAddress addr = ClusteringHelper.selectNextTarget(visited, siblings);
    assertTrue("Address should be null", addr == null);
    assertEquals(5, visited.size());
  }

}