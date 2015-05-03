package org.sapia.corus.client.cli.command.exec;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class RestartByVmIdCommandTest {

  private RestartByVmIdCommand restart;
  
  @Before
  public void setUp() {
    restart = new RestartByVmIdCommand();
  }
  
  @Test
  public void testValidationOption_cluster() {
    restart.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_i() {
    restart.validate(CmdLine.parse("-i i"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    restart.validate(CmdLine.parse("-i"));
  }
  
  @Test
  public void testValidateOption_hard() {
    restart.validate(CmdLine.parse("-hard"));
  }

}
