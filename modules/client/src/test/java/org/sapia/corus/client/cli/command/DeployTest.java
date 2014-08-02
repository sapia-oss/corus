package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class DeployTest {

  private Deploy deploy;
  
  @Before
  public void setUp() {
    deploy = new Deploy();
  }
  
  @Test
  public void testValidateOption_e() {
    deploy.validate(CmdLine.parse("-e e"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_e_no_value() {
    deploy.validate(CmdLine.parse("-e"));
  }

  @Test
  public void testValidateOption_f() {
    deploy.validate(CmdLine.parse("-f f"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_f_no_value() {
    deploy.validate(CmdLine.parse("-f"));
  }
  
  @Test
  public void testValidateOption_s() {
    deploy.validate(CmdLine.parse("-s s"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_s_no_value() {
    deploy.validate(CmdLine.parse("-s"));
  }
  
  @Test
  public void testValidateOption_d() {
    deploy.validate(CmdLine.parse("-d d"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_a_no_value() {
    deploy.validate(CmdLine.parse("-a"));
  }
  
  @Test
  public void testValidateOption_a() {
    deploy.validate(CmdLine.parse("-a a"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    deploy.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_seq() {
    deploy.validate(CmdLine.parse("-seq"));
  }

}
