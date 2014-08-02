package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class CronTest {

  private Cron cron;
  
  @Before
  public void setUp() {
    cron = new Cron();
  }
  
  @Test
  public void testValidateOption_cluster() {
    cron.validate(CmdLine.parse("-cluster"));
  }

  @Test
  public void testValidateOption_i() {
    cron.validate(CmdLine.parse("-i id"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    cron.validate(CmdLine.parse("-i"));
  }

  @Test
  public void testValidateOption_d() {
    cron.validate(CmdLine.parse("-d d"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    cron.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    cron.validate(CmdLine.parse("-v v"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    cron.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_p() {
    cron.validate(CmdLine.parse("-p p"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    cron.validate(CmdLine.parse("-p"));
  }
  
  @Test
  public void testValidateOption_n() {
    cron.validate(CmdLine.parse("-n n"));
  }
  
  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    cron.validate(CmdLine.parse("-n"));
  }
}
