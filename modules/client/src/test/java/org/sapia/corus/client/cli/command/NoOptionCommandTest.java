package org.sapia.corus.client.cli.command;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

public class NoOptionCommandTest {
  
  private NoOptionCommand cmd;
  
  @Before
  public void setUp() {
    cmd = new NoOptionCommand() {
      
      @Override
      protected void doInit(CliContext context) {
      }
      
      @Override
      protected void doExecute(CliContext ctx) throws AbortException,
          InputException {
      }
    };
  }

  @Test
  public void testValidate() {
    cmd.validate(CmdLine.parse("-a -b -c"));
  }

  @Test
  public void testGetAvailableOptions() {
    assertEquals(0, cmd.getAvailableOptions().size());
  }

}
