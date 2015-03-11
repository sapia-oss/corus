package org.sapia.corus.port;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.database.RevId;

public class PortManagerImplTest {
  
  private TestPortManager ports;

  @Before
  public void setUp() throws Exception {
    ports = new TestPortManager();
    ports.addPortRange("single", 10, 10);
    ports.addPortRange("multi", 20, 30);
  }

  @Test
  public void testAquireSinglePort() throws Exception{
    ports.aquirePort("single");
    try{
      ports.aquirePort("single");
      fail("Should not have been able to acquire port");
    }catch(PortUnavailableException e){
      //ok
    }
  }
  
  @Test
  public void testAquireMultiPort() throws Exception{
    ports.aquirePort("multi");
    ports.aquirePort("multi");
  }

  @Test
  public void testReleaseSinglePort() throws Exception{
    int port  = ports.aquirePort("single");
    ports.releasePort("single", port);
    ports.aquirePort("single");
  }
  
  @Test
  public void testReleaseMultiPort() throws Exception{
    int port  = ports.aquirePort("multi");
    ports.releasePort("multi", port);
    assertEquals(20, ports.aquirePort("multi"));
  }

  @Test
  public void testAddOverlappingPortRange() throws Exception{
    try{
      ports.addPortRange("overlap", 10, 30);
      fail("Port range should overlap");
    }catch(PortRangeConflictException e){
      //ok
    }
  }
  
  @Test
  public void testUpdateExistingPortRange() throws Exception {
    ports.updatePortRange("single", 40, 50);
    assertEquals(40, ports.aquirePort("single"));
  }
  
  @Test
  public void testUpdateNonExistingPortRange() throws Exception {
    ports.updatePortRange("single2", 40, 50);
    assertEquals(40, ports.aquirePort("single2"));
  }  

  @Test
  public void testRemovePortRange() throws Exception{
    ports.removePortRange(ArgMatchers.exact("single"), false);
  }
  
  @Test
  public void testRemoveActivePortRange() throws Exception{
    ports.aquirePort("single");
    try{
      ports.removePortRange(ArgMatchers.exact("single"), false);
      fail("Should not have been able to remove port range");
    }catch(PortActiveException e){
      //ok
    }
    
  }
  
  @Test
  public void testArchive() throws Exception {
    ports.archive(RevId.valueOf("123"));
    ports.removePortRange(ArgMatchers.any(), true);
    
    assertEquals(0, ports.getPortRanges().size());
    
    ports.unarchive(RevId.valueOf("123"));
    assertEquals(2, ports.getPortRanges().size());
  }

}
