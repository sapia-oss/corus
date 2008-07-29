package org.sapia.corus.deployer;

import junit.framework.TestCase;

import org.sapia.corus.admin.CommandArgParser;
import org.sapia.corus.deployer.config.Distribution;


/**
 * @author Yanick Duchesne
 * 2002-03-01
 */
public class DistributionStoreTest extends TestCase {
  
  DistributionStore _store;
  
  /**
   * Constructor for DistributionStoreTest.
   * @param arg0
   */
  public DistributionStoreTest(String arg0) {
    super(arg0);
  }
  
  protected void setUp() throws Exception {
    _store = new DistributionStore();
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
    super.assertTrue(_store.containsDistribution("test2", "1.0"));
    super.assertTrue(_store.containsDistribution(
        CommandArgParser.parse("test*"), 
        CommandArgParser.parse("1.0")));    
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
    
    _store.removeDistribution(CommandArgParser.parse("test1"), 
        CommandArgParser.parse("1.0"));
    _store.addDistribution(d);
  }
  
  public void testRemoveDistributionForName() throws Exception {
    _store.removeDistribution(CommandArgParser.parse("test*"), 
        CommandArgParser.parse("1.0"));
    assertEquals(0, _store.getDistributions().size());
  }  
  
  public void testRemoveDistributionForNameAndVersion() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("2.0");
    _store.addDistribution(d);
    
    _store.removeDistribution(CommandArgParser.parse("test1"), 
        CommandArgParser.parse("*"));
    assertEquals(1, _store.getDistributions().size());
  }  

  public void testGetDistributions() throws Exception {
    super.assertEquals(2, _store.getDistributions().size());
  }
  
  public void testGetDistributionsForName() throws Exception{
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("1.0");
    _store.addDistribution(d);
    super.assertEquals(2, _store.getDistributions(CommandArgParser.parse("test*")).size());
  }
  
  public void testGetDistributionsForNameVersion() throws Exception{
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("2.0");
    _store.addDistribution(d);
    super.assertEquals(2, _store.getDistributions(
        CommandArgParser.parse("*"), CommandArgParser.parse("1.0")).size());
  }
  
  
}
