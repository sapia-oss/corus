package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ProcessConfigTest {

  ProcessConfig conf;
  
  @Before
  public void setUp() throws Exception {
    conf = new ProcessConfig();
    Java starter = new Java();
    conf.setName("server");
    starter.setProfile("test");
    conf.addStarter(starter);
    
    starter = new Java();
    conf.setName("server");
    starter.setProfile("prod");
    conf.addStarter(starter);
    Dependency dep = new Dependency();
    dep.setProfile("prod");
    dep.setProcess("db");
    starter.addDependency(dep);
  }

  @Test
  public void testGetTagSet() {
    conf.setTags("tag1, tag2, tag3");
    Assert.assertTrue(conf.getTagSet().contains("tag1"));
    Assert.assertTrue(conf.getTagSet().contains("tag2"));
    Assert.assertTrue(conf.getTagSet().contains("tag3"));
  }

  @Test
  public void testGetDependenciesFor() {
    Assert.assertEquals(0, conf.getDependenciesFor("test").size());
    Assert.assertEquals(1, conf.getDependenciesFor("prod").size());
  }

  @Test
  public void testGetProfiles() {
    Set<String> profiles = new HashSet<String>(conf.getProfiles());
    Assert.assertTrue(profiles.contains("test"));
    Assert.assertTrue(profiles.contains("prod"));
  }

  @Test
  public void testContainsProfile() {
    Assert.assertTrue(conf.containsProfile("test"));
    Assert.assertTrue(conf.containsProfile("prod"));
  }

}
