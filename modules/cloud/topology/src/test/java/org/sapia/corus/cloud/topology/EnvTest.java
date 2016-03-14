package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EnvTest {

  private Topology topo;
  private Env env;
  private EnvTemplate template;
  
  @Before
  public void setUp() {
    topo = new Topology();
    
    env = new Env();
    env.setName("test");
    env.setTemplateRef("template");
    
    template = new EnvTemplate();
    template.setName("template");
    
    Cluster c = new Cluster();
    c.setName("cluster");
    template.addCluster(c);
    
    Region r = new Region();
    r.setName("region");
    template.addRegion(r);
    
    topo.addEnv(env);
  }
  
  @Test
  public void testParam() {
    env.addParam("n1", "v1");
    
    assertTrue(env.existsParam("n1"));
  }
  
  @Test
  public void testParam_topo() {
    topo.addParam("n1", "v1");
    
    assertTrue(env.existsParam("n1"));
  }
  
  @Test
  public void testRender() {
    env.render(TopologyContext.newInstance().addEnvTemplate(template));
    assertEquals(1, env.getClusters().size());
    assertEquals(1, env.getRegions().size());
  }

}
