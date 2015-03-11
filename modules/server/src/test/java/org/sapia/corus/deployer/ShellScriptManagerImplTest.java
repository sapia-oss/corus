package org.sapia.corus.deployer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.client.services.file.FileSystemModule;

@RunWith(MockitoJUnitRunner.class)
public class ShellScriptManagerImplTest {
  
  @Mock
  private FileSystemModule fileSystem;
  
  @Mock
  private DeployerConfiguration config;
  
  @Mock
  private ShellScriptDatabase database;
  
  private ShellScriptManagerImpl manager;

  @Before
  public void setUp() {
    manager = new ShellScriptManagerImpl();
    manager.setDatabase(database);
    manager.setDeployerConfig(config);
    manager.setFileSystem(fileSystem);
  }

  @Test
  public void testGetScripts() {
    List<ShellScript> scripts = mock(List.class);
    when(database.getScripts()).thenReturn(scripts);
    manager.getScripts();
    verify(database).getScripts();
  }

  @Test
  public void testGetScriptsWithCriteria() {
    List<ShellScript> scripts = mock(List.class);
    when(database.getScripts()).thenReturn(scripts);
    manager.getScripts(ShellScriptCriteria.newInstance().setAlias(ArgMatchers.parse("*")));
    verify(database).getScripts(any(ShellScriptCriteria.class));
  }

  @Test
  public void testRemoveScript() throws Exception {
    List<ShellScript> existing = new ArrayList<ShellScript>();
    existing.add(new ShellScript("test", "testFile", "testDesc"));
    
    when(database.removeScript(any(ShellScriptCriteria.class))).thenReturn(existing);
    when(config.getScriptDir()).thenReturn("scriptDir");

    manager.removeScripts(ShellScriptCriteria.newInstance().setAlias(ArgMatchers.parse("test")));
    
    verify(config).getScriptDir();
    verify(fileSystem).deleteFile(any(File.class));
    verify(database).removeScript(any(ShellScriptCriteria.class));
  }

  @Test
  public void testAddScript() throws Exception {
    List<ShellScript> existing = new ArrayList<ShellScript>();
    existing.add(new ShellScript("test", "testFile", "testDesc"));
    
    manager.addScript(new ShellScript("test", "testFile", "testDesc"), mock(File.class));
    
    verify(database).addScript(any(ShellScript.class));
  }

}
