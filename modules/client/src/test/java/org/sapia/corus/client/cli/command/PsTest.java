package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class PsTest {

  private Ps ps;
  
  @Before
  public void setUp() {
    ps = new Ps();
  }
  
  @Test
  public void testValidateOption_cluster() {
    ps.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_clean() {
    ps.validate(CmdLine.parse("-clean"));
  }
  
  @Test
  public void testValidateOption_d() {
    ps.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    ps.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    ps.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    ps.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    ps.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    ps.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    ps.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    ps.validate(CmdLine.parse("-p"));
  }


  @Test
  public void testValidateOption_i() {
    ps.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    ps.validate(CmdLine.parse("-i"));
  }

  @Test
  public void testValidateOption_ports() {
    ps.validate(CmdLine.parse("-ports"));
  }
}
