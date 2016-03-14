package org.sapia.corus.cloud.topology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ClusterTest {

  private Env     env;
  private Cluster cluster;
  
  @Before
  public void setUp() {
    env     = new Env();
    env.setName("test-env");
    
    cluster = new Cluster();
    cluster.setInstances(10);
    cluster.setName("test");
    cluster.setTemplateRef("template");
    
    env.addCluster(cluster);
  }
  
  @Test
  public void testParam_cluster() {
    cluster.addParam("n1", "v1");
    assertTrue(cluster.existsParam("n1"));
  }
  
  @Test
  public void testParam_env() {
    env.addParam("n1", "v1");
    assertTrue(cluster.existsParam("n1"));
  }
  
  @Test
  public void testRender_instances_set() {
    ClusterTemplate template = new ClusterTemplate();
    template.setName("template");
    template.setInstances(5);
 
    Machine m = new Machine();
    m.setImageId("testImage");
    m.setMaxInstances(1);
    m.setMinInstances(1);
    m.setName("testMachine");
    template.addMachine(m);
    cluster.render(TopologyContext.newInstance().addClusterTemplate(template));
    
    assertEquals(1, cluster.getMachines().size());
    assertEquals(10, cluster.getInstances());
  }

  @Test
  public void testRender_instances_not_set() {
    cluster.setInstances(-1);
    ClusterTemplate template = new ClusterTemplate();
    template.setName("template");
    template.setInstances(5);
 
    Machine m = new Machine();
    m.setImageId("testImage");
    m.setMaxInstances(1);
    m.setMinInstances(1);
    m.setName("testMachine");
    template.addMachine(m);
    cluster.render(TopologyContext.newInstance().addClusterTemplate(template));
    
    assertEquals(1, cluster.getMachines().size());
    assertEquals(5, cluster.getInstances());
  }
}
