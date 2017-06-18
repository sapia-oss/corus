package org.sapia.corus.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ClusterInfoTest {

  @Mock
  private CorusConnectionContext context;
  
  @Mock
  private PublicKey pubKey;
  
  private ServerAddress realLocalAddress;
  
  @Before
  public void setUp() {
    realLocalAddress = HttpAddress.newDefaultInstance("currentHost", 1);
    when(context.getAddress()).thenReturn(realLocalAddress);
    when(context.getServerHost()).thenReturn(
        CorusHost.newInstance(
            "test", 
            new Endpoint(realLocalAddress, realLocalAddress), 
            "test-os", "test-jvm", pubKey
        )
    );

  }
  
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
  
  @Test
  public void testConvert_with_targets_copy() {
    ClusterInfo c = new ClusterInfo(true);
    c.addTargets(Collects
      .arrayToSet(
        (ServerAddress) HttpAddress.newDefaultInstance("host0", 0),
        (ServerAddress) HttpAddress.newDefaultInstance("localhost", 1)
      )
    );
    
    ClusterInfo copy = c.convertLocalHost(context);
    
    assertTrue(copy.isClustered());
    assertEquals(2, copy.getTargets().size());
    assertTrue(copy.getTargets().contains(realLocalAddress));
  }
  
  @Test
  public void testConvert_with_excluded_copy() {
    ClusterInfo c = new ClusterInfo(true);
    c.addExcluded(HttpAddress.newDefaultInstance("host0", 0));
    c.addExcluded(HttpAddress.newDefaultInstance("localhost", 1));
    
    ClusterInfo copy = c.convertLocalHost(context);
    
    assertTrue(copy.isClustered());
    assertEquals(2, copy.getExcluded().size());
    assertTrue(copy.getExcluded().contains(realLocalAddress));
    
  }

}
