package org.sapia.corus.deployer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public class DistributionDatabaseTest {
  
  private DistributionDatabase store;
  
  @Before
  public void setUp() throws Exception {
    store = new DistributionDatabaseImpl();
    Distribution      d1 = new Distribution();
    Distribution      d2 = new Distribution();
    d1.setName("test1");
    d1.setVersion("1.0");
    d2.setName("test2");
    d2.setVersion("1.0");
    store.addDistribution(d1);
    store.addDistribution(d2);

  }

  @Test
  public void testContainsDistribution() throws Exception {
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test2")
      .version("1.0")
      .build();
    
    assertTrue(store.containsDistribution(criteria));
    
    criteria = DistributionCriteria.builder()
      .name("test*")
      .version("1.0")
      .build();
    
    assertTrue(store.containsDistribution(criteria));    
  }

  @Test
  public void testAddDuplicate() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("1.0");
    try {
      store.addDistribution(d);
      throw new Exception("DuplicateDistributionException should have been thrown");
    } catch (DuplicateDistributionException e) {
      // ok
    }
  }

  @Test
  public void testRemoveDistribution() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("1.0");
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test1")
      .version("1.0")
      .build();    
    
    store.removeDistribution(criteria);
    store.addDistribution(d);
  }
  
  @Test
  public void testRemoveDistributionForName() throws Exception {
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test*")
      .version("1.0")
      .build();    
    store.removeDistribution(criteria);
    assertEquals(0, store.getDistributions(DistributionCriteria.builder().all()).size());
  }  
  
  @Test
  public void testRemoveDistributionForNameAndVersion() throws Exception {
    Distribution      d = new Distribution();
    d.setName("test1");
    d.setVersion("2.0");
    store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test1")
      .version("*")
      .build();        
    
    store.removeDistribution(criteria);
    assertEquals(1, store.getDistributions(DistributionCriteria.builder().all()).size());
  }  
  
  @Test
  public void testRemoveDistributionForNameAndVersion_backup() throws Exception {
    store = new DistributionDatabaseImpl();

    for (int i = 0; i < 5; i++) {
      Distribution      d = new Distribution();
      d.setName("test1");
      d.setVersion("2." + i);
      store.addDistribution(d);
    }
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test1")
      .version("*")
      .backup(2)
      .build();        
    
    store.removeDistribution(criteria);
    List<Distribution> dists = store.getDistributions(DistributionCriteria.builder().all());

    assertEquals(2, dists.size());

    int v = 3;
    for (Distribution d : dists) {
      assertEquals("2." + v, d.getVersion());
      v++;
    }
  }  
  
  @Test
  public void testRemoveDistributions_backup() throws Exception {
    store = new DistributionDatabaseImpl();

    for (int n = 0; n < 2; n++) {
      for (int i = 0; i < 5; i++) {
        Distribution      d = new Distribution();
        d.setName("test" + n);
        d.setVersion("2." + i);
        store.addDistribution(d);
      }
    }
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test*")
      .version("*")
      .backup(2)
      .build();        
    
    store.removeDistribution(criteria);
    List<Distribution> dists = store.getDistributions(DistributionCriteria.builder().all());

    assertEquals(4, dists.size());
    
    for (int n = 0; n < 2; n++) {
      int v = 3;
      for (int i = 0; i < 2; i++) {
        assertEquals("2." + v, dists.get(i).getVersion());
        v++;
      }
    }
  }  

  @Test
  public void testGetDistributions() throws Exception {
    assertEquals(2, store.getDistributions(DistributionCriteria.builder().all()).size());
  }
  
  @Test
  public void testGetDistributionsForName() throws Exception {
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("1.0");
    store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("test*")
      .build();        
    
    assertEquals(2, store.getDistributions(criteria).size());
  }
  
  @Test
  public void testGetDistributionsForNameVersion() throws Exception{
    Distribution      d = new Distribution();
    d.setName("dist");
    d.setVersion("2.0");
    store.addDistribution(d);
    
    DistributionCriteria criteria = DistributionCriteria.builder()
      .name("*")
      .version("1.0")
      .build();        
    
    assertEquals(2, store.getDistributions(criteria).size());
  }
  
  
}
