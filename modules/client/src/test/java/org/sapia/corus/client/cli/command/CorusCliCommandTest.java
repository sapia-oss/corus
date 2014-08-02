package org.sapia.corus.client.cli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class CorusCliCommandTest {

  @Mock
  private CliContext context;
  
  private CorusCliCommand cmd;
  
  @Before
  public void setUp() {
    cmd = new CorusCliCommand() {
      @Override
      protected void doExecute(CliContext ctx) throws AbortException {
      }
    
      @Override
      protected List<OptionDef> getAvailableOptions() {
        return Collects.arrayToList(new OptionDef("a", true), new OptionDef("b", false));
      }
    };
  }
  
  @Test
  public void testGetClusterInfo_is_clustered() {
    setUpCommand(CmdLine.parse("-cluster"));
    assertTrue(cmd.getClusterInfo(context).isClustered());
  }
  
  @Test
  public void testGetClusterInfo_is_not_clustered() {
    setUpCommand(CmdLine.parse("-a"));
    assertFalse(cmd.getClusterInfo(context).isClustered());
  }
  
  @Test
  public void testGetClusterInfo_is_clustered_targeted() {
    setUpCommand(CmdLine.parse("-cluster h1:1,h2:2"));
    Set<ServerAddress> addresses = cmd.getClusterInfo(context).getTargets();
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("h1", 1)));
    assertTrue(addresses.contains(HttpAddress.newDefaultInstance("h2", 2)));
  }

  @Test
  public void testGetOpt_option_with_name_and_default_value() {
    setUpCommand(CmdLine.parse("-a"));
    String actual = CorusCliCommand.getOpt(context, "a", "test").getValue();
    assertEquals("test", actual);
    
    setUpCommand(CmdLine.parse("-a foo"));
    actual = CorusCliCommand.getOpt(context, "a", "test").getValue();
    assertEquals("foo", actual);
  }

  @Test
  public void testGetOpt_option_not_set() {
    setUpCommand(CmdLine.parse("-b"));
    assertNull(CorusCliCommand.getOpt(context, "a"));
  }
  
  @Test
  public void testGetOpt_option() {
    setUpCommand(CmdLine.parse("-a 1"));
    assertEquals("1", CorusCliCommand.getOpt(context, "a").getValue());
  }

  @Test
  public void testGetOptValue() {
    setUpCommand(CmdLine.parse("-a 1"));
    assertEquals("1", CorusCliCommand.getOptValue(context, "a"));
  }

  @Test
  public void testGetOptValues() {
    setUpCommand(CmdLine.parse("-a 1,2,3,4,5"));
    List<Integer> values = CorusCliCommand.getOptValues(context, "a", new Func<Integer, String>() {
      @Override
      public Integer call(String arg) {
        return Integer.parseInt(arg);
      }
    });
    assertEquals(5, values.size());
    assertTrue(values.containsAll(Collects.arrayToList(1, 2, 3, 4, 5)));
  }

  @Test
  public void testGetFirstArg() {
    setUpCommand(CmdLine.parse("arg1 -a 1"));
    assertEquals("arg1", cmd.getFirstArg(context).getName());
  }

  @Test
  public void testValidate() {
    cmd.validate(CmdLine.parse("-a 1 -b"));
  }
  
  @Test(expected = InputException.class)
  public void testValidate_unknown_option() {
    cmd.validate(CmdLine.parse("-a 1 -c"));
  }
  
  @Test(expected = InputException.class)
  public void testValidate_option_with_unspecified_value() {
    cmd.validate(CmdLine.parse("-a"));
  }
  
  private void setUpCommand(CmdLine cmd) {
    when(context.getCommandLine()).thenReturn(cmd);
  }

}
