package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.*;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Localhost;

public class CorusHostTest {
  
  private Endpoint ep;
  private CorusHost host;
  
  @Before
  public void setUp() {
    ep = new Endpoint(new TCPAddress("tcp", "host", 1), new TCPAddress("tcp", "host", 2));
    host = CorusHost.newInstance(ep, "linux", "jvm");
  }


  @Test
  public void testGetEndpoint() {
    assertEquals(ep.getServerAddress(),  host.getEndpoint().getServerAddress());
    assertEquals(ep.getChannelAddress(),  host.getEndpoint().getChannelAddress());
  }

  @Test
  public void testGetOsInfo() {
    assertEquals("linux", host.getOsInfo());
  }

  @Test
  public void testSetOsInfo() {
    host.setOsInfo("windows");
    assertEquals("windows", host.getOsInfo());
  }

  @Test
  public void testGetJavaVmInfo() {
    assertEquals("jvm", host.getJavaVmInfo());
  }

  @Test
  public void testSetJavaVmInfo() {
    host.setJavaVmInfo("jvm2");
    assertEquals("jvm2", host.getJavaVmInfo());
  }

  @Test
  public void testGetRepoRole() {
    host.setRepoRole(RepoRole.CLIENT);
    assertEquals(RepoRole.CLIENT, host.getRepoRole());
  }

  @Test
  public void testSetHostName() {
    host.setHostName("test-host");
    assertEquals("test-host", host.getHostName());
  }

  @Test
  public void testGetHostName() throws Exception {
    assertEquals(Localhost.getPreferredLocalAddress().getHostName(), host.getHostName());
  }

  @Test
  public void testGetFormattedAddress() {
    host.setHostName("host");
    assertEquals("host:1", host.getFormattedAddress());
  }

  @Test
  public void testGetHostNameComparator() {
  }

  @Test
  public void testToJson() {
  }

  @Test
  public void testSerialization() throws Exception {
    byte[] payload = SerializationUtils.serialize(host);
    CorusHost copy = (CorusHost) SerializationUtils.deserialize(payload);
    assertEquals(host, copy);
  }


  @Test
  public void testToString() {
    System.out.println(host.toString());
  }

}
