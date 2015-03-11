package org.sapia.corus.deployer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.database.InMemoryDbMap;

public class ShellScriptDatabaseImplTest {
  
  private ShellScriptDatabaseImpl db;
  
  @Before
  public void setUp() {
    db = new ShellScriptDatabaseImpl(new InMemoryDbMap<String, ShellScript>(new ClassDescriptor<ShellScript>(ShellScript.class)));
  }

  @Test
  public void testAddScript() {
    ShellScript s = new ShellScript("test", "test_file", "test_desc");
    db.addScript(s);
    assertEquals(s, db.getScripts(new ShellScriptCriteria().setAlias(ArgMatchers.parse("test"))).get(0));
  }

  @Test
  public void testRemoveScriptByAlias() {
    ShellScript s = new ShellScript("test", "test_file", "test_desc");
    db.addScript(s);
    db.removeScript("test");
    assertTrue("ShellScript not removed", db.getScripts().isEmpty());
  }

  @Test
  public void testRemoveScriptByCriteria() {
    ShellScript s = new ShellScript("test", "test_file", "test_desc");
    db.addScript(s);
    List<ShellScript> removed = db.removeScript(new ShellScriptCriteria().setAlias(ArgMatchers.parse("test")));
    assertTrue("ShellScript not removed", db.getScripts().isEmpty());
    assertEquals(s, removed.get(0));    
  }
  
  @Test
  public void testGetScript() throws Exception {
    ShellScript s = new ShellScript("test", "test_file", "test_desc");
    db.addScript(s);
    db.getScript("test");
  }
  
  @Test(expected = ScriptNotFoundException.class)
  public void testGetScriptNotFound() throws Exception {
    ShellScript s = new ShellScript("test", "test_file", "test_desc");
    db.addScript(s);
    db.getScript("test1");
  }  

}
