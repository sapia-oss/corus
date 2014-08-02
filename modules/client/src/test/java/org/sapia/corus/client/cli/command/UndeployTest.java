package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class UndeployTest {

  private Undeploy undeploy;
  
  @Before
  public void setUp() {
    undeploy = new Undeploy();
  }
  
  @Test
  public void testValidateOption_e() {
    undeploy.validate(CmdLine.parse("-e e"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_e_no_option() {
    undeploy.validate(CmdLine.parse("-e"));
  }
  
  @Test
  public void testValidateOption_s() {
    undeploy.validate(CmdLine.parse("-s s"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_s_no_option() {
    undeploy.validate(CmdLine.parse("-s"));
  }

  @Test
  public void testValidateOption_f() {
    undeploy.validate(CmdLine.parse("-f f"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_f_no_option() {
    undeploy.validate(CmdLine.parse("-f"));
  }
}
