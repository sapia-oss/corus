package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.console.Option;
import org.sapia.corus.client.common.Env;

@RunWith(MockitoJUnitRunner.class)
public class IncludeProcessPropertiesArgTest {
  
  @Mock
  private Env env;
  
  @Test
  public void testGenerate_simpleValues() {
    when(env.getProperties()).thenReturn(new Property[] {
        new Property("prop1", "val1"),
        new Property("prop2", "val2")
    });
    
    IncludeProcessPropertiesArg arg = new IncludeProcessPropertiesArg();
    CmdLine cmd = new CmdLine();
    
    arg.generate(env, cmd);
    
    Option opt1 = (Option) cmd.get(0);
    Option opt2 = (Option) cmd.get(1);
    
    assertEquals("Dprop1=val1", opt1.getName());
    assertEquals("Dprop2=val2", opt2.getName());
  }

  @Test
  public void testGenerate_quotedValues() {
    when(env.getProperties()).thenReturn(new Property[] {
        new Property("prop1", "\" val1\""),
        new Property("prop2", "\"val 2\"")
    });
    
    IncludeProcessPropertiesArg arg = new IncludeProcessPropertiesArg();
    CmdLine cmd = new CmdLine();
    
    arg.generate(env, cmd);
    
    Option opt1 = (Option) cmd.get(0);
    Option opt2 = (Option) cmd.get(1);
    
    assertEquals("Dprop1=\" val1\"", opt1.getName());
    assertEquals("Dprop2=\"val 2\"", opt2.getName());
  }

  @Test
  public void testGenerate_containsSpace() {
    when(env.getProperties()).thenReturn(new Property[] {
        new Property("prop1", "val1 "),
        new Property("prop2", "val 2")
    });
    
    IncludeProcessPropertiesArg arg = new IncludeProcessPropertiesArg();
    CmdLine cmd = new CmdLine();
    
    arg.generate(env, cmd);
    
    Option opt1 = (Option) cmd.get(0);
    Option opt2 = (Option) cmd.get(1);
    
    assertEquals("Dprop1=\"val1 \"", opt1.getName());
    assertEquals("Dprop2=\"val 2\"", opt2.getName());
  }

}
