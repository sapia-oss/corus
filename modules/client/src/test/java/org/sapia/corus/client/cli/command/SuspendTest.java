package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

@RunWith(MockitoJUnitRunner.class)
public class SuspendTest {

  private Suspend suspend;
  
  @Before
  public void setUp() {
    suspend = new Suspend();
  }
  
  @Test
  public void testValidateOption_cluster() {
    suspend.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    suspend.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    suspend.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    suspend.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    suspend.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    suspend.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    suspend.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    suspend.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    suspend.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_i() {
    suspend.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    suspend.validate(CmdLine.parse("-i"));
  }
  
  @Test
  public void testValidateOption_op() {
    suspend.validate(CmdLine.parse("-op o"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_op_no_value() {
    suspend.validate(CmdLine.parse("-op"));
  }
  
  @Test
  public void testValidateOption_w() {
    suspend.validate(CmdLine.parse("-w"));
  }

  @Test
  public void testValidateOption_hard() {
    suspend.validate(CmdLine.parse("-hard"));
  }
}
