package org.sapia.corus.client.services.deployer.dist;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.exceptions.misc.MissingDataException;

public class BaseJavaStarterTest {

  private BaseJavaStarter starter;

  @Before
  public void setUp() {
    starter = new TestJavaStarter();
  }

  @Test
  public void testArg() throws Exception {
    VmArg arg = new VmArg();
    arg.setValue("-javaaagent:${testAgent}");
    starter.addArg(arg);
    Env env = mock(Env.class);
    when(env.getProperties()).thenReturn(new Property[] { new Property("testAgent", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    Arg a = (Arg) cmd.get(1);
    assertEquals("-javaaagent:test", a.getName());
  }

  @Test
  public void testAddProperty() {
    Property prop = new Property();
    prop.setName("someProp");
    prop.setValue("${someValue}");
    starter.addProperty(prop);
    Env env = mock(Env.class);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("DsomeProp=test", o.getName());

  }

  @Test
  public void testAddOption() {
    Option opt = new Option();
    opt.setName("someOption");
    opt.setValue("${someValue}");
    starter.addOption(opt);
    Env env = mock(Env.class);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("someOption", o.getName());
    assertEquals("test", o.getValue());
  }

  @Test
  public void testAddXoption() {
    XOption opt = new XOption();
    opt.setName("someOption");
    opt.setValue("${someValue}");
    starter.addXoption(opt);
    Env env = mock(Env.class);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("XsomeOptiontest", o.getName());
  }

  static class TestJavaStarter extends BaseJavaStarter {

    @Override
    public CmdLine toCmdLine(Env env) throws MissingDataException {
      return super.buildCommandLine(env).command;
    }
  }
}
