package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class StatusTest extends CorusCliCommandTestSupport {

  private Status status;
  
  @Before
  public void setUp() {
    status = new Status();
  }
  
  @Test
  public void testValidateOption_cluster() {
    status.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    status.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    status.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    status.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    status.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    status.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    status.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    status.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    status.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_i() {
    status.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    status.validate(CmdLine.parse("-i"));
  }

}
