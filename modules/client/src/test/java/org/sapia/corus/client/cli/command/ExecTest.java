package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class ExecTest {
  
  private Exec exec;
  
  @Before
  public void setUp() {
    exec = new Exec();
  }
  
  @Test
  public void testValidateOption_cluster() {
    exec.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_clean() {
    exec.validate(CmdLine.parse("-clean"));
  }

  @Test
  public void testValidateOption_e() {
    exec.validate(CmdLine.parse("-e e"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_e_no_value() {
    exec.validate(CmdLine.parse("-e"));
  }
  
  @Test
  public void testValidateOption_s() {
    exec.validate(CmdLine.parse("-s s"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_s_no_value() {
    exec.validate(CmdLine.parse("-s"));
  }
  
  @Test
  public void testValidateOption_d() {
    exec.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    exec.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    exec.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    exec.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    exec.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    exec.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    exec.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    exec.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_i() {
    exec.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    exec.validate(CmdLine.parse("-i"));
  }
  
  @Test
  public void testValidateOption_w() {
    exec.validate(CmdLine.parse("-w"));
  }
}
