package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

public class ResumeTest {

  private Resume resume;
  
  @Before
  public void setUp() {
    resume = new Resume();
  }
  
  @Test
  public void testValidateOption_cluster() {
    resume.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_d() {
    resume.validate(CmdLine.parse("-d d"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_d_no_value() {
    resume.validate(CmdLine.parse("-d"));
  }
  
  @Test
  public void testValidateOption_v() {
    resume.validate(CmdLine.parse("-v v"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_v_no_value() {
    resume.validate(CmdLine.parse("-v"));
  }
  
  @Test
  public void testValidateOption_n() {
    resume.validate(CmdLine.parse("-n n"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_n_no_value() {
    resume.validate(CmdLine.parse("-n"));
  }
  
  @Test
  public void testValidateOption_p() {
    resume.validate(CmdLine.parse("-p p"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_p_no_value() {
    resume.validate(CmdLine.parse("-p"));
  }

  @Test
  public void testValidateOption_i() {
    resume.validate(CmdLine.parse("-i i"));
  }

  @Test(expected = InputException.class)
  public void testValidateOption_i_no_value() {
    resume.validate(CmdLine.parse("-i"));
  }
  

}
