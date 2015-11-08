package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;

@RunWith(MockitoJUnitRunner.class)
public class GenericTest {
  
  @Mock
  private Env env;
  
  private Generic starter;

  @Before
  public void setUp() {
    starter = new Generic();
    starter.setProfile("test");
    when(env.getProperties()).thenReturn(new Property[] {
        new Property("prop1", "val1"),
        new Property("prop2", "val2")
    });
    when(env.getCommonDir()).thenReturn("testDir");
    when(env.getEnvironmentVariables()).thenReturn(new HashMap<String, String>());
  }
  
  @Test
  public void testCreateArg() {
    starter.createArg().setValue("arg0");
    starter.createArg().setValue("arg1");
    
    CmdLine cmd = starter.toCmdLine(env).getCommand();
    assertEquals("arg0", cmd.get(0).getName());
    assertEquals("arg1", cmd.get(1).getName());
  }

  @Test
  public void testRenderArg() {
    starter.createArg().setValue("${prop1}");
    starter.createArg().setValue("${prop2}");
    
    CmdLine cmd = starter.toCmdLine(env).getCommand();
   
    assertEquals("val1", cmd.get(0).getName());
    assertEquals("val2", cmd.get(1).getName());
  }
  
  @Test
  public void testCreateProcessProperties() {
    starter.createProcessProperties();
    
    CmdLine cmd = starter.toCmdLine(env).getCommand();
    
    assertEquals("Dprop1=val1", cmd.get(0).getName());
    assertEquals("Dprop2=val2", cmd.get(1).getName());
  }

}
