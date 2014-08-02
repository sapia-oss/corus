package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class KillTest extends CorusCliCommandTestSupport {

  private Kill kill;
  
  @Before
  public void setUp() {
    kill = new Kill();
  }
  
  @Test
  public void testValidateOption_cluster() {
    kill.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    kill.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    kill.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    kill.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    kill.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    kill.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    kill.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    kill.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    kill.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_i() {
    kill.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    kill.validate(CmdLine.parse("-i"));
  }
  
  @Test
  public void testValidateOption_op() {
    kill.validate(CmdLine.parse("-op o"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_op_no_value() {
    kill.validate(CmdLine.parse("-op"));
  }
  
  @Test
  public void testValidateOption_w() {
    kill.validate(CmdLine.parse("-w"));
  }

  @Test
  public void testValidateOption_hard() {
    kill.validate(CmdLine.parse("-hard"));
  }
}
