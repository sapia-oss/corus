package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class LsTest extends CorusCliCommandTestSupport {

  private Ls ls;
  
  @Before
  public void setUp() {
    ls = new Ls();
  }
  
  @Test
  public void testValidateOption_cluster() {
    ls.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    ls.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    ls.validate(CmdLine.parse("-d"));
  }

  @Test
  public void testValidateOption_v() {
    ls.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    ls.validate(CmdLine.parse("-v"));
  }

  @Test
  public void testValidateOption_e() {
    ls.validate(CmdLine.parse("-e e"));
  }

  @Test
  public void testValidateOption_e_no_value() {
    ls.validate(CmdLine.parse("-e"));
  }
  
  @Test
  public void testValidateOption_f() {
    ls.validate(CmdLine.parse("-f f"));
  }

  @Test
  public void testValidateOption_f_no_value() {
    ls.validate(CmdLine.parse("-f"));
  }
  
  @Test
  public void testValidateOption_s() {
    ls.validate(CmdLine.parse("-s s"));
  }

  @Test
  public void testValidateOption_s_no_value() {
    ls.validate(CmdLine.parse("-s"));
  }
}
