package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class ScriptTest extends CorusCliCommandTestSupport {
  
  private Script script;

  @Before
  public void setUp() {
    script = new Script();
  }
  
  @Test
  public void testValidateOption_e() {
    script.validate(CmdLine.parse("-e e"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_e_no_value() {
    script.validate(CmdLine.parse("-e"));
  }
  
  @Test
  public void testValidateOption_i() {
    script.validate(CmdLine.parse("-e e"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    script.validate(CmdLine.parse("-e"));
  }
}
