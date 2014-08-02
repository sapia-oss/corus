package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class RippleTest {

  private Ripple ripple;
  
  @Before
  public void setUp() {
    ripple = new Ripple();
  }
  
  @Test
  public void testValidateOption_s() {
    ripple.validate(CmdLine.parse("-s s"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_s_no_value() {
    ripple.validate(CmdLine.parse("-s"));
  }
  
  @Test
  public void testValidateOption_c() {
    ripple.validate(CmdLine.parse("-c c"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_c_no_value() {
    ripple.validate(CmdLine.parse("-c"));
  }
  
  @Test
  public void testValidateOption_m() {
    ripple.validate(CmdLine.parse("-m m"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_m_no_value() {
    ripple.validate(CmdLine.parse("-m"));
  }
  
  @Test
  public void testValidateOption_b() {
    ripple.validate(CmdLine.parse("-b b"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_b_no_value() {
    ripple.validate(CmdLine.parse("-b"));
  }
}
