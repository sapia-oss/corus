package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;

@RunWith(MockitoJUnitRunner.class)
public class GenericArgTest {

  @Mock
  private Env env;
  
  @Test
  public void testGenerate() {
    GenericArg config = new GenericArg();
    config.setValue("test");
    CmdLine cmd = new CmdLine();
    config.generate(env, cmd);
    
    Arg arg = (Arg) cmd.get(0);
    assertEquals("test", arg.getName());
  }

}
