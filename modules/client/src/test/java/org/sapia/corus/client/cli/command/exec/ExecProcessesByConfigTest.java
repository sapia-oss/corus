package org.sapia.corus.client.cli.command.exec;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class ExecProcessesByConfigTest {
  
  private ExecProcessesByConfig exec;
  
  @Before
  public void setUp() {
    exec = new ExecProcessesByConfig();
  }

  @Test
  public void testValidationOption_e() {
    exec.validate(CmdLine.parse("-e e"));
  }

  @Test(expected = InputException.class)
  public void testValidationOption_e_no_value() {
    exec.validate(CmdLine.parse("-e"));
  }
}
