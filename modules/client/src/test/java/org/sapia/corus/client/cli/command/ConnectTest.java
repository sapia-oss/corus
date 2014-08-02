package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class ConnectTest extends CorusCliCommandTestSupport {
  
  private Connect connect;

  @Before
  public void setUp() {
    connect = new Connect();
  }
  
  @Test
  public void testValidateOption_h() {
    connect.validate(CmdLine.parse("-h host"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_h_no_value() {
    connect.validate(CmdLine.parse("-h"));
  }
  
  @Test
  public void testValidateOption_p() {
    connect.validate(CmdLine.parse("-p port"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    connect.validate(CmdLine.parse("-p"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_unknown() {
    connect.validate(CmdLine.parse("-foo"));
  }
}
