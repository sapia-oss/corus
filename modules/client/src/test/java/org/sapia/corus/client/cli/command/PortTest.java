package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class PortTest extends CorusCliCommandTestSupport {

  private Port port;
  
  @Before
  public void setUp() {
    port = new Port();
  }
  
  @Test
  public void testValidateOption_cluster() {
    port.validate(CmdLine.parse("-cluster"));
  }

  @Test
  public void testValidateOption_n() {
    port.validate(CmdLine.parse("-n n"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    port.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    port.validate(CmdLine.parse("-p p"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    port.validate(CmdLine.parse("-p"));
  }
  
  @Test
  public void testValidateOption_f_no_value() {
    port.validate(CmdLine.parse("-f"));
  }

  @Test
  public void testValidateOption_clear_no_value() {
    port.validate(CmdLine.parse("-clear"));
  }
  
  @Test
  public void testValidateOption_min() {
    port.validate(CmdLine.parse("-min m"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_min_no_value() {
    port.validate(CmdLine.parse("-min"));
  }
  
  @Test
  public void testValidateOption_max() {
    port.validate(CmdLine.parse("-max m"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_max_no_value() {
    port.validate(CmdLine.parse("-max"));
  }
}
