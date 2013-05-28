package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Serialization;

public class EndpointTest {

  private Endpoint ep1, ep2;
  
  @Before
  public void setUp() {
    ep1 = new Endpoint(
        new TCPAddress("test", "localhost", 1000), 
        new TCPAddress("test", "localhost", 1001)
    );
    
    ep2 = new Endpoint(
        new TCPAddress("test", "localhost", 2000), 
        new TCPAddress("test", "localhost", 2001)
    );
  }
   
  
  @Test
  public void testEquals() {
    assertEquals(ep1, ep1);
  }
  
  @Test
  public void testNotEquals() {
    assertNotSame(ep1, ep2);
  }

  @Test
  public void testSerialization() throws Exception{
    byte[] content = Serialization.serialize(ep1);
    ep1 = (Endpoint) Serialization.deserialize(content);
    assertEquals(ep1, ep1);
  }

}
