package org.sapia.corus.client.sort;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;

public class ProcessComparatorsTest {

  @Test
  public void testForName() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p2", "prof", "1", "1");
    
    assertTrue(ProcessComparators.forName().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forName().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forName().compare(p1, p1) == 0);
  }

  @Test
  public void testForDistribution() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d2", "v1", "p1", "prof", "1", "1");
    
    assertTrue(ProcessComparators.forDistribution().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forDistribution().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forDistribution().compare(p1, p1) == 0);
  }

  @Test
  public void testForVersion() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v2", "p1", "prof", "1", "1");
    
    assertTrue(ProcessComparators.forVersion().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forVersion().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forVersion().compare(p1, p1) == 0);  
  }

  @Test
  public void testForId() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "2", "1");
    
    assertTrue(ProcessComparators.forId().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forId().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forId().compare(p1, p1) == 0);  
  }

  @Test
  public void testForOsPid() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "1", "2");
    
    assertTrue(ProcessComparators.forOsPid().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forOsPid().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forOsPid().compare(p1, p1) == 0); 
  }

  @Test
  public void testForProfile() {
    Process p1 = process("d1", "v1", "p1", "prof1", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof2", "1", "1");
    
    assertTrue(ProcessComparators.forProfile().compare(p1, p2) < 0);
    assertTrue(ProcessComparators.forProfile().compare(p2, p1) > 0);
    assertTrue(ProcessComparators.forProfile().compare(p1, p1) == 0); 
  }

  
  private Process process(String dist, String version, String name, String profile, String id, String osPid) {
    Process p = new Process(new DistributionInfo(dist, version, profile, name), id);
    p.setOsPid(osPid);
    return p;
  }
}
