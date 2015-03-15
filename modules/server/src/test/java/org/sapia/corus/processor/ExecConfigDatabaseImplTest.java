package org.sapia.corus.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.database.InMemoryDbMap;

public class ExecConfigDatabaseImplTest {
  
  private DbMap<String, ExecConfig> map;
  private ExecConfigDatabaseImpl    db;
  
  @Before
  public void setUp() throws Exception {
    map = new InMemoryDbMap<String, ExecConfig>(new ClassDescriptor<ExecConfig>(ExecConfig.class));
    db = new ExecConfigDatabaseImpl(map);
    for (int i = 0; i < 5; i++) {
      ExecConfig c = new ExecConfig();
      c.setName("test-" + i);
      if (i < 2) {
        c.setStartOnBoot(true);
      }
      db.addConfig(c);
    }
  }

  @Test
  public void testGetConfigs() {
    assertEquals(5, db.getConfigs().size());
  }

  @Test
  public void testGetBootstrapConfigs() {
    assertEquals(2, db.getBootstrapConfigs().size());
  }

  @Test
  public void testGetConfigsFor() {
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgMatchers.parse("test-0")).build();
    assertEquals(1, db.getConfigsFor(criteria).size());
  }

  @Test
  public void testRemoveConfigsFor() {
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgMatchers.parse("test-0")).build();
    db.removeConfigsFor(criteria);
    assertEquals(4, db.getConfigs().size());
  }
  
  @Test
  public void testRemoveConfigsFor_backup() {
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgMatchers.parse("test*")).backup(1).build();
    db.removeConfigsFor(criteria);
    assertEquals(1, db.getConfigs().size());
    assertEquals("test-4", db.getConfigFor("test-4").getName());
  }

  @Test
  public void testGetConfigFor() {
    assertEquals("test-0", db.getConfigFor("test-0").getName());
  }

  @Test
  public void testRemoveConfig() {
    db.removeConfig("test-0");
    assertNull(db.getConfigFor("test0-"));
  }
  
  @Test
  public void testArchive() {
    db.archiveExecConfigs(RevId.valueOf("rev"));
    db.removeConfigsFor(ExecConfigCriteria.builder().name(ArgMatchers.parse("*")).build());
    assertEquals(0, db.getConfigs().size());
    
    db.unarchiveExecConfigs(RevId.valueOf("rev"));
    assertEquals(5, db.getConfigs().size());
  }
  
  @Test
  public void testArchive_clear_previous_rev() {
    db.archiveExecConfigs(RevId.valueOf("rev"));
    db.removeConfigsFor(ExecConfigCriteria.builder().name(ArgMatchers.parse("*")).build());
    
    ExecConfig newConf = new ExecConfig();
    newConf.setName("newConf");
    db.addConfig(newConf);
    db.archiveExecConfigs(RevId.valueOf("rev"));
    db.removeConfigsFor(ExecConfigCriteria.builder().name(ArgMatchers.parse("*")).build());

    db.unarchiveExecConfigs(RevId.valueOf("rev"));
    assertEquals(1, db.getConfigs().size());
    assertEquals("newConf", db.getConfigs().get(0).getName());
  }

}
