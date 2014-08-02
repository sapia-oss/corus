package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class ConfTest extends CorusCliCommandTestSupport {

  private Conf conf;
  
  @Before
  public void setUp() {
    conf = new Conf();
  }
  
  @Test
  public void testValidateOption_cluster() {
    conf.validate(CmdLine.parse("-cluster"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_unknown() {
    conf.validate(CmdLine.parse("-xyz"));
  }
  
  @Test
  public void testValidateOption_p() {
    conf.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_t() {
    conf.validate(CmdLine.parse("-t"));
  }
  
  @Test
  public void testValidateOption_s() {
    conf.validate(CmdLine.parse("-s"));
  }
  
  @Test
  public void testValidateOption_clear() {
    conf.validate(CmdLine.parse("-clear"));
  }
  
  @Test
  public void testValidateOption_f() {
    conf.validate(CmdLine.parse("-f file"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_f_no_value() {
    conf.validate(CmdLine.parse("-f"));
  }
}
