package org.sapia.corus.port;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Processor;

public class PortManagerImplTest {
  
  private TestPortManager ports;
  
  private Processor processor;
  private List<org.sapia.corus.client.services.processor.Process> activeProcesses = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    processor = Mockito.mock(Processor.class);
    Mockito.when(processor.getProcessesWithPorts()).thenAnswer(i -> activeProcesses);
    
    ports = new TestPortManager(processor);
    ports.addPortRange("single", 10, 10);
    ports.addPortRange(new PortRange("multi", 20, 30));
  }

  private void doSimulateProcessCreationForPort(String rangeName, int port) {
    org.sapia.corus.client.services.processor.Process p = new org.sapia.corus.client.services.processor.Process();
    p.addActivePort(new ActivePort(rangeName, port));
    activeProcesses.add(p);
  }

  private void doSimulateProcessTerminationForPort(String rangeName, int port) {
    org.sapia.corus.client.services.processor.Process target = null;
    for (Iterator<org.sapia.corus.client.services.processor.Process> it = activeProcesses.iterator(); it.hasNext() && target == null; ) {
      target = it.next();
      if (!rangeName.equals(target.getActivePorts().get(0).getName()) || port != target.getActivePorts().get(0).getPort()) {
        target = null;
      }
    }
    if (target != null) {
      activeProcesses.remove(target);
    }
  }
  
  @Test(expected = PortUnavailableException.class)
  public void testAquireSinglePort() throws Exception {
    int acquired = ports.aquirePort("single");
    doSimulateProcessCreationForPort("single", acquired);

    ports.aquirePort("single");
  }
  
  @Test
  public void testAquireMultiPort_twoPorts() throws Exception{
    int acquired1 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired1);
    assertEquals(20, acquired1);

    int acquired2 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired2);
    assertEquals(21, acquired2);
  }
  
  @Test
  public void testAquireMultiPort_withRelease() throws Exception{
    int acquired1 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired1);
    assertEquals(20, acquired1);

    int acquired2 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired2);
    assertEquals(21, acquired2);

    int acquired3 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired3);
    assertEquals(22, acquired3);
    
    doSimulateProcessTerminationForPort("multi", acquired2);
    
    int acquired4 = ports.aquirePort("multi");
    doSimulateProcessCreationForPort("multi", acquired4);
    assertEquals(21, acquired4);
  }

  @Test
  public void testAddConsecutivePortRange() throws Exception{
    ports.addPortRange("range1", 100, 105);
    assertEquals(1, ports.getPortRanges(ArgMatchers.exact("range1")).size());

    ports.addPortRange("range2", 106, 110);
    assertEquals(1, ports.getPortRanges(ArgMatchers.exact("range2")).size());
  }

  @Test(expected = PortRangeConflictException.class)
  public void testAddOverlapping1PortRange() throws Exception{
    ports.addPortRange("overlap", 9, 11);
  }

  @Test(expected = PortRangeConflictException.class)
  public void testAddOverlapping2PortRange() throws Exception{
    ports.addPortRange("overlap", 15, 25);
  }

  @Test(expected = PortRangeConflictException.class)
  public void testAddOverlapping3PortRange() throws Exception{
    ports.addPortRange("overlap", 25, 35);
  }

  @Test(expected = PortRangeConflictException.class)
  public void testAddExistingPortRange() throws Exception{
    ports.addPortRange("single", 100, 100);
  }

  @Test(expected = PortRangeInvalidException.class)
  public void testAddInvalidPortRange() throws Exception{
    ports.addPortRange("invalid", 30, 10);
  }
  
  @Test
  public void testUpdateExistingPortRange() throws Exception {
    ports.updatePortRange("single", 40, 50);
    assertEquals(40, ports.aquirePort("single"));
  }
  
  @Test(expected = PortRangeInvalidException.class)
  public void testUpdateInvalidPortRange() throws Exception {
    ports.updatePortRange("single", 50, 40);
  }
  
  @Test
  public void testUpdateNonExistingPortRange() throws Exception {
    ports.updatePortRange("single2", 40, 50);
    assertEquals(40, ports.aquirePort("single2"));
  }  

  @Test
  public void testRemovePortRange() throws Exception{
    ports.removePortRange(ArgMatchers.exact("single"), false);
    assertEquals(0, ports.getPortRanges(ArgMatchers.exact("single")).size());
  }
  
  @Test(expected = PortActiveException.class)
  public void testRemoveActivePortRange() throws Exception{
    int acquired = ports.aquirePort("single");
    doSimulateProcessCreationForPort("single", acquired);

    ports.removePortRange(ArgMatchers.exact("single"), false);
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
