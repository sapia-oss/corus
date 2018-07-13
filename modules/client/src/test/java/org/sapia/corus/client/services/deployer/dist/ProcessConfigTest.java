package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.facade.CorusConnector;

@RunWith(MockitoJUnitRunner.class)
public class ProcessConfigTest {

  private ProcessConfig conf;
  
  @Mock
  private Env           env;
  
  @Before
  public void setUp() throws Exception {
    conf = new ProcessConfig();
    Java starter = new Java();
    conf.setName("server");
    starter.setProfile("test");
    starter.setNumaEnabled(false);
    conf.addStarter(starter);

    starter = new Java();
    conf.setName("server");
    starter.setProfile("prod");
    starter.setNumaEnabled(true);
    conf.addStarter(starter);
    Dependency dep = new Dependency();
    dep.setProfile("prod");
    dep.setProcess("db");
    starter.addDependency(dep);
  }

  @Test
  public void testGetTagSet() {
    conf.setTags("tag1, tag2, tag3");
    assertTrue(conf.getTagSet().contains("tag1"));
    assertTrue(conf.getTagSet().contains("tag2"));
    assertTrue(conf.getTagSet().contains("tag3"));
  }

  @Test
  public void testGetDependenciesFor() {
    assertEquals(0, conf.getDependenciesFor("test").size());
    assertEquals(1, conf.getDependenciesFor("prod").size());
  }

  @Test
  public void testGetProfiles() {
    Set<String> profiles = new HashSet<String>(conf.getProfiles());
    assertTrue(profiles.contains("test"));
    assertTrue(profiles.contains("prod"));
  }

  @Test
  public void testContainsProfile() {
    assertTrue(conf.containsProfile("test"));
    assertTrue(conf.containsProfile("prod"));
  }
  
  @Test
  public void testMatchesProfile() {
    assertTrue(conf.matches(Matcheable.DefaultPattern.parse("test")));
    assertFalse(conf.matches(Matcheable.DefaultPattern.parse("foo")));
  }
  
  @Test
  public void testMatchesProcessName() {
    assertTrue(conf.matches(Matcheable.DefaultPattern.parse("server")));
    assertFalse(conf.matches(Matcheable.DefaultPattern.parse("foo")));
  }
  
  @Test
  public void testIsNumaEnabled_active() {
    when(env.getProfile()).thenReturn("test");
    boolean actual = conf.isNumaEnabled(env);
    
    assertFalse("Invalid value for isNumaEnabled", actual);
  }
  
  @Test
  public void testIsNumaEnabled_inactive() {
    when(env.getProfile()).thenReturn("prod");
    boolean actual = conf.isNumaEnabled(env);
    
    assertTrue("Invalid value for isNumaEnabled", actual);
  }
  
  @Test
  public void testIsNumaEnabled_invalidProfile() {
    when(env.getProfile()).thenReturn("invalid");
    boolean actual = conf.isNumaEnabled(env);
    
    assertFalse("Invalid value for isNumaEnabled", actual);
  }
  
  @Test
  public void testPreExec() throws Throwable {
    PreExec exec = new PreExec();
    
    Cmd     cmd1  = new Cmd();
    cmd1.setValue("test ${process.property}");
    exec.addCmd(cmd1);
    
    Cmd     cmd2  = new Cmd();
    cmd2.setValue("test ${user.dir}");
    exec.addCmd(cmd2);

    Cmd     cmd3  = new Cmd();
    cmd3.setValue("test ${env.property}");
    exec.addCmd(cmd3);
    
    Cmd     cmd4  = new Cmd();
    cmd4.setValue("test ${unresolved.property}");
    exec.addCmd(cmd4);
    
    conf.setPreExec(exec);
    
    CorusConnector connector = mock(CorusConnector.class);
    Interpreter    itr       = new Interpreter(connector);
    TestCommand    command   = new TestCommand();
    itr.getCommands().addCommand("test", command);

    when(env.getInterpreter()).thenReturn(itr);
    when(env.getProperties()).thenReturn(new Property[] { new Property("process.property", "process.property.value") });
    
    Map<String, String> envVariables = new HashMap<>();
    envVariables.put("env.property", "env.property.value");
    
    when(env.getEnvironmentVariables()).thenReturn(envVariables);
    conf.preExec(env);

    assertEquals(4, command.cmdLines.size());
    
    assertEquals("Could not resolve process property", "process.property.value", command.cmdLines.get(0).toString());
    assertEquals("Could not resolve system property", System.getProperty("user.dir"), command.cmdLines.get(1).toString());
    assertEquals("Could not resolve environment property", "env.property.value", command.cmdLines.get(2).toString());
    assertEquals("Could not resolve environment property", "${unresolved.property}", command.cmdLines.get(3).toString());
  }
  
  private class TestCommand extends CorusCliCommand {
    
    private List<CmdLine> cmdLines = new ArrayList<>(); 
    
    @Override
    protected void doExecute(CliContext ctx) throws AbortException,
        InputException {
      this.cmdLines.add(ctx.getCommandLine());
    }
    
    @Override
    protected void doInit(CliContext context) {
    }
    
    @Override
    public List<OptionDef> getAvailableOptions() {
      return new ArrayList<>();
    }
  }

}
