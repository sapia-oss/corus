package org.sapia.corus.client.cli.command.exec;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;

public class RestartAllCommandTest {
  
  private RestartAllCommand restartAll;
  
  @Before
  public void setUp() {
    restartAll = new RestartAllCommand();
  }

  @Test
  public void testValidateOption_cluster() {
    restartAll.validate(CmdLine.parse("-cluster"));
  }
  
  @Test
  public void testValidateOption_w() {
    restartAll.validate(CmdLine.parse("-w"));
  }

  @Test
  public void testValidateOption_hard() {
    restartAll.validate(CmdLine.parse("-hard"));
  }

}
