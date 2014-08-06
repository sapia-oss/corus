package org.sapia.corus.client.cli.command.exec;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class RestartByOsPidCommandTest {
  
  private RestartByOsPidCommand restart;

  @Before
  public void setUp() {
    restart = new RestartByOsPidCommand();
  }

  @Test
  public void testValidateOption_op() {
    restart.validate(CmdLine.parse("-op pid"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_op_no_value() {
    restart.validate(CmdLine.parse("-op"));
  }
  
  @Test
  public void testValidateOption_hard() {
    restart.validate(CmdLine.parse("-hard"));
  }

}
