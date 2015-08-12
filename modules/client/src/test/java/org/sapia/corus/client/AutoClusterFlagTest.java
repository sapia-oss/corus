package org.sapia.corus.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AutoClusterFlagTest {

  AutoClusterFlag all, allNotClustered, notAll, notClustered;
  
  @Before
  public void setUp() throws Exception {
    all = AutoClusterFlag.forAll(ClusterInfo.clustered());
    allNotClustered = AutoClusterFlag.forAll(ClusterInfo.notClustered());
    notAll = AutoClusterFlag.forExplicit(ClusterInfo.clustered());
    notClustered = AutoClusterFlag.notClustered();
  }

  @Test
  public void testGetClusterInfo() {
    assertNotNull(all.getClusterInfo());
  }

  @Test
  public void testIsAll_forAll_clustered() {
    assertTrue(all.isAll());
  }

  @Test
  public void testIsAll_forAll_not_clustered() {
    assertFalse(allNotClustered.isAll());
  }
  
  @Test
  public void testIsAll_forExplicit_clustered() {
    assertFalse(notAll.isAll());
  }
  
  @Test
  public void testNotClustered() {
    assertFalse(notClustered.isAll());
  }

}
