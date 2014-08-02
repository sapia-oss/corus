package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class HttpTest extends CorusCliCommandTestSupport {

  private Http http;
  
  @Before
  public void setUp() {
    http = new Http();
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_clustered() {
    http.validate(CmdLine.parse("-clustered"));
  }
  
  @Test
  public void testValidateOption_u() {
    http.validate(CmdLine.parse("-u u"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_u_no_value() {
    http.validate(CmdLine.parse("-u"));
  }

  @Test
  public void testValidateOption_m() {
    http.validate(CmdLine.parse("-m m"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_m_no_value() {
    http.validate(CmdLine.parse("-m"));
  }

  @Test
  public void testValidateOption_t() {
    http.validate(CmdLine.parse("-t t"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_t_no_value() {
    http.validate(CmdLine.parse("-t"));
  }
  
  @Test
  public void testValidateOption_s() {
    http.validate(CmdLine.parse("-s s"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_s_no_value() {
    http.validate(CmdLine.parse("-s"));
  }
  
  @Test
  public void testValidateOption_p() {
    http.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    http.validate(CmdLine.parse("-p"));
  }
  
  @Test
  public void testValidateOption_c() {
    http.validate(CmdLine.parse("-c c"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_c_no_value() {
    http.validate(CmdLine.parse("-c"));
  }
  
  @Test
  public void testValidateOption_x() {
    http.validate(CmdLine.parse("-x x"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_x_no_value() {
    http.validate(CmdLine.parse("-x"));
  }
}
