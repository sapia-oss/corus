package org.sapia.corus.client.cli;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sapia.console.CommandNotFoundException;

public class CommandDictTest {
  
  @Test
  public void testHasCommandFor() {
    for (String n : CommandDict.getCommandNames()) {
      assertTrue(CommandDict.hasCommandFor(n));
    }
  }

  @Test
  public void testInstantiateCommandFor() throws Exception {
    for (String n : CommandDict.getCommandNames()) {
      CommandDict.instantiateCommandFor(n);
    }
  }
  
  @Test(expected = CommandNotFoundException.class)
  public void testInstantiateCommandFor_unknown_command() throws Exception {
    CommandDict.instantiateCommandFor("xxx");
  }

  @Test
  public void testGetCommandClassFor() throws Exception {
    for (String n : CommandDict.getCommandNames()) {
      CommandDict.getCommandClassFor(n);
    }
  }

  @Test(expected = CommandNotFoundException.class)
  public void testGetCommandClassFor_unknown_command() throws Exception {
    CommandDict.getCommandClassFor("xxx");
   }

}
