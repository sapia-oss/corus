package org.sapia.corus.client;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collects;

public class ClusterInfoTest {

  @Test
  public void testIsClustered() {
    ClusterInfo c = new ClusterInfo(true);
    assertTrue(c.isClustered());
  }

  @Test
  public void testAddTargetsSetOfServerAddress() {
    ClusterInfo c = new ClusterInfo(true);
    c.addTargets(Collects.arrayToSet((ServerAddress) HttpAddress.newDefaultInstance("host0", 0)));
    assertEquals(1, c.getTargets().size());
  }

  @Test
  public void testIsTargetingAllHosts() {
    ClusterInfo c = new ClusterInfo(true);
    assertTrue(c.isTargetingAllHosts());
  }

  @Test
  public void testIsTargetingAllHosts_false() {
    ClusterInfo c = new ClusterInfo(true);
    c.addTarget(HttpAddress.newDefaultInstance("host0", 0));
    assertFalse(c.isTargetingAllHosts());
  }
  
  @Test
  public void testGetTargets() {
    ClusterInfo c = new ClusterInfo(true);
    c.addTarget(HttpAddress.newDefaultInstance("host0", 0));
    assertEquals(1, c.getTargets().size());
  }

  @Test
  public void testToLiteralForm() {
    String expected = "host0:0,host1:1";
    ClusterInfo c = new ClusterInfo(true);
    c.addTarget(HttpAddress.newDefaultInstance("host0", 0));
    c.addTarget(HttpAddress.newDefaultInstance("host1", 1));
    assertEquals(expected, c.toLiteralForm());
  }

  @Test
  public void testFromLiteralForm() {
    String literal = "host0:0,host1:1";
    ClusterInfo c = ClusterInfo.fromLiteralForm(literal);
    c.getTargets().contains(HttpAddress.newDefaultInstance("host0", 0));
    c.getTargets().contains(HttpAddress.newDefaultInstance("host1", 1));

  }

}
