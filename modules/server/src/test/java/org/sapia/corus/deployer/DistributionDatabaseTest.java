package org.sapia.corus.deployer;

import junit.framework.TestCase;

import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;


/**
 * @author Yanick Duchesne
 * 2002-03-01
 */
public class DistributionDatabaseTest extends TestCase {
  
  DistributionDatabase _store;
  
  /**
   * Constructor for DistributionStoreTest.
   * @param arg0
   */
  public DistributionDatabaseTest(String arg0) {
    super(arg0);
  }
  
  protected void setUp() throws Exception {
    _store = new DistributionDatabaseImpl();
    Distribution      d1 = new Distribution();
    Distribution      d2 = new Distribution();
    d1.setName("test1");
    d1.setVersion("1.0");
    d2.setName("test2");
    d2.setVersion("1.0");
    _store.addDistribution(d1);
    _store.addDistribution(d2);

  }

  public void testContainsDistribution() throws Exception {
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test2")
      .version("1.0")
      .build();
    
    super.assertTrue(_store.containsDistribution(criteria));
    
    criteria = DistributionCriteria.builder()
      .name("test*")
      .version("1.0")
      .build();
    
    super.assertTrue(_store.containsDistribution(criteria));    
  }

  public void testAddDuplicate() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("1.0");
    try {
      _store.addDistribution(d);
      throw new Exception("DuplicateDistributionException should have been thrown");
    } catch (DuplicateDistributionException e) {
      // ok
    }
  }

  public void testRemoveDistribution() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("1.0");
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test1")
      .version("1.0")
      .build();    
    
    _store.removeDistribution(criteria);
    _store.addDistribution(d);
  }
  
  public void testRemoveDistributionForName() throws Exception {
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test*")
      .version("1.0")
      .build();    
    _store.removeDistribution(criteria);
    assertEquals(0, _store.getDistributions(DistributionCriteria.builder().all()).size());
  }  
  
  public void testRemoveDistributionForNameAndVersion() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("2.0");
    _store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test1")
      .version("*")
      .build();        
    
    _store.removeDistribution(criteria);
    assertEquals(1, _store.getDistributions(DistributionCriteria.builder().all()).size());
  }  

  public void testGetDistributions() throws Exception {
    super.assertEquals(2, _store.getDistributions(DistributionCriteria.builder().all()).size());
  }
  
  public void testGetDistributionsForName() throws Exception{
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("1.0");
    _store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test*")
      .build();        
    
    super.assertEquals(2, _store.getDistributions(criteria).size());
  }
  
  public void testGetDistributionsForNameVersion() throws Exception{
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("2.0");
    _store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("*")
      .version("1.0")
      .build();        
    
    super.assertEquals(2, _store.getDistributions(criteria).size());
  }
  
  
}
