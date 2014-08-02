package org.sapia.corus.client.cli.command;

import org.junit.Before;
import org.junit.Test;
import org.sapia.console.CmdLine;

public class PullTest {

  private Pull pull;
  
  @Before
  public void setUp() {
    pull = new Pull();
  }
  
  @Test
  public void testValidateOption_cluster() {
    pull.validate(CmdLine.parse("-cluster"));
  }
  
}
