package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class RestartTest {

  private Restart restart;
  
  @Before
  public void setUp() {
    restart = new Restart();
  }
  
  @Test
  public void testValidateOption_cluster() {
    restart.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    restart.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    restart.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    restart.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    restart.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    restart.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    restart.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    restart.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    restart.validate(CmdLine.parse("-p"));
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
  public void testValidateOption_op() {
    restart.validate(CmdLine.parse("-op o"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_op_no_value() {
    restart.validate(CmdLine.parse("-op"));
  }
  
  @Test
  public void testValidateOption_w() {
    restart.validate(CmdLine.parse("-w"));
  }

  @Test
  public void testValidateOption_hard() {
    restart.validate(CmdLine.parse("-hard"));
  }
}
