package org.sapia.corus.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.database.HashDbMap;

public class ExecConfigDatabaseImplTest {
  
  private DbMap<String, ExecConfig> map;
  private ExecConfigDatabaseImpl    db;
  
  @Before
  public void setUp() throws Exception {
    map = new HashDbMap<String, ExecConfig>(new ClassDescriptor<ExecConfig>(ExecConfig.class));
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
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgFactory.parse("test-0")).build();
    assertEquals(1, db.getConfigsFor(criteria).size());
  }

  @Test
  public void testRemoveConfigsFor() {
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgFactory.parse("test-0")).build();
    db.removeConfigsFor(criteria);
    assertEquals(4, db.getConfigs().size());
  }
  
  @Test
  public void testRemoveConfigsFor_backup() {
    ExecConfigCriteria criteria = ExecConfigCriteria.builder().name(ArgFactory.parse("test*")).backup(1).build();
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

}
