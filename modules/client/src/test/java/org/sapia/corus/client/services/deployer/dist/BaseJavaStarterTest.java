package org.sapia.corus.client.services.deployer.dist;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.FileSystemFacade;
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.common.StrLookups;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.services.deployer.dist.BaseJavaStarter.CmdLineBuildResult;

@RunWith(MockitoJUnitRunner.class)
public class BaseJavaStarterTest {
  
  @Mock
  private Env env;
  
  @Mock 
  private FileSystemFacade fileSystem; 
  
  @Mock
  private File file;
  
  @Mock
  private PathFilter pathFilter;

  private BaseJavaStarter starter;

  @Before
  public void setUp() {
    starter = new TestJavaStarter();
    when(env.createPathFilter(anyString())).thenReturn(pathFilter);
    when(env.getFileSystem()).thenReturn(fileSystem);
    when(file.exists()).thenReturn(true);
    when(fileSystem.getFile(anyString())).thenReturn(file);
  }

  @Test
  public void testArg() throws Exception {
    VmArg arg = new VmArg();
    arg.setValue("-javaaagent:${testAgent}");
    starter.addArg(arg);
    when(env.getProperties()).thenReturn(new Property[] { new Property("testAgent", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    Arg a = (Arg) cmd.get(1);
    assertEquals("-javaaagent:test", a.getName());
  }

  @Test
  public void testAddProperty() {
    Property prop = new Property();
    prop.setName("someProp");
    prop.setValue("${someValue}");
    starter.addProperty(prop);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("DsomeProp=test", o.getName());

  }

  @Test
  public void testAddOption() {
    Option opt = new Option();
    opt.setName("someOption");
    opt.setValue("${someValue}");
    starter.addOption(opt);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("someOption", o.getName());
    assertEquals("test", o.getValue());
  }

  @Test
  public void testAddXoption() {
    XOption opt = new XOption();
    opt.setName("someOption");
    opt.setValue("${someValue}");
    starter.addXoption(opt);
    when(env.getProperties()).thenReturn(new Property[] { new Property("someValue", "test") });
    CmdLine cmd = starter.toCmdLine(env);
    org.sapia.console.Option o = (org.sapia.console.Option) cmd.get(1);
    assertEquals("XsomeOptiontest", o.getName());
  }
  
  @Test
  public void testGetOptionalCp_classes_relative_dir() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    
    String libDirs = "${relative.dir}/classes/";
    String cp = starter.getOptionalCp(libDirs, StrLookups.forKeyValues("relative.dir", "test-relative-dir"), env);
    
    assertEquals("test-common-dir/test-relative-dir/classes/", cp);
  }
  
  @Test
  public void testGetOptionalCp_classes_absolute_dir() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    
    String libDirs = "${absolute.dir}/classes/";
    String cp = starter.getOptionalCp(libDirs, StrLookups.forKeyValues("absolute.dir", "/test-absolute-dir"), env);
    
    assertEquals("/test-absolute-dir/classes/", cp);
  }
  
  @Test
  public void testGetOptionalCp_lib_relative_dir() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(pathFilter.filter()).thenReturn(new String[] { "test-jar0.jar", "test-jar1.jar"});

    String libDirs = "${relative.dir}/test-lib";
    String cp = starter.getOptionalCp(libDirs, StrLookups.forKeyValues("relative.dir", "test-relative-dir"), env);
    
    String p1 = FilePath.newInstance()
        .addDir("test-common-dir")
        .addDir("test-relative-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar0.jar")
        .createFilePath();
    
    String p2 = FilePath.newInstance()
        .addDir("test-common-dir")
        .addDir("test-relative-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar1.jar")
        .createFilePath();
    
    assertEquals(FileUtils.mergeFilePaths(p1, p2), cp);
  }
  
  @Test
  public void testGetOptionalCp_lib_absolute_dir() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(pathFilter.filter()).thenReturn(new String[] { "test-jar0.jar", "test-jar1.jar"});
    
    String libDirs = "${absolute.dir}/test-lib";
    String cp = starter.getOptionalCp(libDirs, StrLookups.forKeyValues("absolute.dir", "/test-absolute-dir"), env);
    
    String p1 = "/" + FilePath.newInstance()
        .addDir("test-absolute-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar0.jar")
        .createFilePath();
    
    String p2 = "/" + FilePath.newInstance()
        .addDir("test-absolute-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar1.jar")
        .createFilePath();
    
    assertEquals(FileUtils.mergeFilePaths(p1, p2), cp);
  }
  
  @Test
  public void testGetOptionalCp_mixed_paths() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(pathFilter.filter()).thenReturn(new String[] { "test-jar0.jar", "test-jar1.jar"});
    
    String libDirs = "${absolute.dir}/classes/;${relative.dir}/classes/:${absolute.dir}/test-lib;${relative.dir}/test-lib" ;

    String cp = starter.getOptionalCp(libDirs, StrLookups.forKeyValues("relative.dir", "test-relative-dir", "absolute.dir", "/test-absolute-dir"), env);

    String p1 = "/test-absolute-dir/classes/".replace("/", File.separator);

    String p2 = "test-common-dir/test-relative-dir/classes/".replace("/", File.separator);

    String p3 = "/" + FilePath.newInstance()
        .addDir("test-absolute-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar0.jar")
        .createFilePath();

    String p4 = "/" + FilePath.newInstance()
        .addDir("test-absolute-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar1.jar")
        .createFilePath();
    
    String p5 = FilePath.newInstance()
        .addDir("test-common-dir")
        .addDir("test-relative-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar0.jar")
        .createFilePath();   
    
    String p6 = FilePath.newInstance()
        .addDir("test-common-dir")
        .addDir("test-relative-dir")
        .addDir("test-lib")
        .setRelativeFile("test-jar1.jar")
        .createFilePath();
    
    assertEquals(FileUtils.mergeFilePaths(p1, p2, p3, p4, p5, p6), cp);

  }
  
  @Test
  public void testBuildCommandLine() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(file.getAbsolutePath()).thenReturn("test-path");
    
    when(env.getProperties()).thenReturn(new Property[] { new Property("test.property", "test.property.value")});
    starter.setJavaHome("test-java-home");
    
    CmdLineBuildResult result = starter.buildCommandLine(env);
    assertEquals("test-path/bin/java -Dtest.property=test.property.value", result.command.toString());
  }
  
  @Test
  public void testBuildCommandLine_vm_type() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(file.getAbsolutePath()).thenReturn("test-path");
    
    when(env.getProperties()).thenReturn(new Property[] { new Property("test.property", "test.property.value")});
    starter.setVmType("server");
    
    CmdLineBuildResult result = starter.buildCommandLine(env);
    assertEquals("test-path/bin/java -server -Dtest.property=test.property.value", result.command.toString());
  }
  
  @Test
  public void testBuildCommandLine_vm_args() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(file.getAbsolutePath()).thenReturn("test-path");
    
    when(env.getProperties()).thenReturn(new Property[] { 
        new Property("test.arg0", "-XXarg0"),
        new Property("test.arg1", "-XXarg1"),
        new Property("test.property", "test.property.value")});
    
    VmArg arg0 = new VmArg();
    arg0.setValue("${test.arg0}");
    starter.addArg(arg0);

    VmArg arg1 = new VmArg();
    arg1.setValue("${test.arg1}");
    starter.addArg(arg1);

    
    CmdLineBuildResult result = starter.buildCommandLine(env);
    System.out.println(result.command.toString());
    assertEquals("test-path/bin/java -XXarg0 -XXarg1 -Dtest.arg0=-XXarg0 -Dtest.arg1=-XXarg1 -Dtest.property=test.property.value", result.command.toString());
  }
  
  @Test
  public void testBuildCommandLine_xoptions() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(file.getAbsolutePath()).thenReturn("test-path");
    when(env.getProperties()).thenReturn(new Property[] { 
        new Property("test.xmx", "5g"), 
        new Property("test.xms", "1g"), 
        new Property("test.property", "test.property.value")
    });
    
    XOption opt0 = new XOption();
    opt0.setName("mx");
    opt0.setValue("${test.xmx}");
    starter.addXoption(opt0);
    
    XOption opt1 = new XOption();
    opt1.setName("ms");
    opt1.setValue("${test.xms}");
    starter.addXoption(opt1);

    CmdLineBuildResult result = starter.buildCommandLine(env);
    assertEquals("test-path/bin/java -Xmx5g -Xms1g -Dtest.xmx=5g -Dtest.xms=1g -Dtest.property=test.property.value", result.command.toString());
  }
  
  @Test
  public void testBuildCommandLine_options() {
    when(env.getCommonDir()).thenReturn("test-common-dir");
    when(file.getAbsolutePath()).thenReturn("test-path");
    when(env.getProperties()).thenReturn(new Property[] { 
        new Property("test.opt0", "o0-value"), 
        new Property("test.opt1", "o1-value"), 
        new Property("test.property", "test.property.value")
    });
    
    Option opt0 = new Option();
    opt0.setName("o0");
    opt0.setValue("${test.opt0}");
    starter.addOption(opt0);
    
    Option opt1 = new Option();
    opt1.setName("o1");
    opt1.setValue("${test.opt1}");
    starter.addOption(opt1);
    
    CmdLineBuildResult result = starter.buildCommandLine(env);
    assertEquals("test-path/bin/java -o0 o0-value -o1 o1-value -Dtest.opt0=o0-value -Dtest.opt1=o1-value -Dtest.property=test.property.value", result.command.toString());
  }

  static class TestJavaStarter extends BaseJavaStarter {

    @Override
    public CmdLine toCmdLine(Env env) throws MissingDataException {
      return super.buildCommandLine(env).command;
    }
  }
}
