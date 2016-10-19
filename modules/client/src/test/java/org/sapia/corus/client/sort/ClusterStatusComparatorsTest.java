package org.sapia.corus.client.sort;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.security.PublicKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ClusterStatusComparatorsTest {

  @Mock
  private ServerAddress channelAddress;

  @Test
  public void testForHostName() {
    ClusterStatus s1 = status("h1", 1000);
    ClusterStatus s2 = status("h2", 1000);
    
    assertTrue(ClusterStatusComparators.forHostName().compare(s1, s2) < 0);
    assertTrue(ClusterStatusComparators.forHostName().compare(s2, s1) > 0);
    assertTrue(ClusterStatusComparators.forHostName().compare(s1, s1) == 0);  
  }

  @Test
  public void testForHostIp() {
    ClusterStatus s1 = status("h1", 1000);
    ClusterStatus s2 = status("h2", 1000);
    
    assertTrue(ClusterStatusComparators.forHostName().compare(s1, s2) < 0);
    assertTrue(ClusterStatusComparators.forHostName().compare(s2, s1) > 0);
    assertTrue(ClusterStatusComparators.forHostName().compare(s1, s1) == 0);  
  }
  
  private ClusterStatus status(String host, int port) {
    CorusHost h = CorusHost.newInstance("test-node", new Endpoint(new TCPAddress("test", host, port), channelAddress), "", "", mock(PublicKey.class));
    h.setHostName(host);
    return new ClusterStatus(h, 0);
  }

}
