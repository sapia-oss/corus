package org.sapia.corus.client.sort;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.security.PublicKey;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class HostComparatorsTest {
  
  @Mock
  private ServerAddress channelAddress;

  @Test
  public void testForIp() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    assertTrue(HostComparators.forIp().compare(h1, h2) < 0);
    assertTrue(HostComparators.forIp().compare(h2, h1) > 0);
    assertTrue(HostComparators.forIp().compare(h1, h1) == 0);  
  }

  @Test
  public void testForName() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    assertTrue(HostComparators.forName().compare(h1, h2) < 0);
    assertTrue(HostComparators.forName().compare(h2, h1) > 0);
    assertTrue(HostComparators.forName().compare(h1, h1) == 0);  
  }

  @Test
  public void testForRole() {
    CorusHost h1 = host(RepoRole.CLIENT, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h1", 1000);
    
    assertTrue(HostComparators.forRole().compare(h1, h2) < 0);
    assertTrue(HostComparators.forRole().compare(h2, h1) > 0);
    assertTrue(HostComparators.forRole().compare(h1, h1) == 0);  
  }

  private CorusHost host(RepoRole role, String host, int port) {
    CorusHost h = CorusHost.newInstance("test-node", new Endpoint(new TCPAddress("test", host, port), channelAddress), "", "", mock(PublicKey.class));
    h.setRepoRole(role);
    h.setHostName(host);
    return h;
  }
}
