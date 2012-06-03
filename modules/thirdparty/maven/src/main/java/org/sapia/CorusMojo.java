package org.sapia;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.client.cli.CorusCli;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.ubik.rmi.server.Hub;

/**
 * Runs a Corus command script against a specified Corus instance.
 * 
 * @goal run
 * 
 * @phase package
 */
public class CorusMojo extends AbstractMojo {
  
  /**
   * The host of the Corus server to which to connect.
   * 
   * @parameter expression="${host}"
   * @required
   */
  private String host;
  
  /**
   * The port of the Corus server to which to connect.
   * 
   * @parameter expression="${port}"
   */
  private int port = CorusCli.DEFAULT_PORT ;
  
  /**
   * Location of the script file to execute.
   * 
   * @parameter expression="${scriptFile}"
   * @required
   */
  private File scriptFile;
  
  /**
   * The maven project.
   * 
   * @parameter expression="${project}"
   * @readonly
   */
  private MavenProject project;
  
  /**
   * Optional properties to pass as to the script.
   * 
   * @parameter
   */
  private Properties scriptProperties;
  
  public void execute() throws MojoExecutionException {
    if (host == null) {
      throw new MojoExecutionException("host not specified");
    }
    
    if (scriptFile == null) {
      throw new MojoExecutionException("scriptFile not specified");
    }
    
    if (!scriptFile.exists()) {
      throw new MojoExecutionException("scriptFile does not exist: " + scriptFile.getAbsolutePath());
    }

    CorusConnectionContext context = null;
    try {
      context    = new CorusConnectionContext(host, port);
    } catch (Exception e) {
      throw new MojoExecutionException(String.format("Could not connect to Corus daemon at %s:%s", host, port), e);
    }
    try {
      CorusConnector connection = new CorusConnector(context);      
      Map<String, String> vars = new HashMap<String, String>();
      vars.put("project.groupId", project.getGroupId());
      vars.put("project.artifactId", project.getArtifactId());
      vars.put("project.description", project.getDescription());
      vars.put("project.id", project.getId());
      vars.put("project.inceptionYear", project.getInceptionYear());
      vars.put("project.name", project.getName());
      vars.put("project.version", project.getVersion());      
      vars.put("project.baseDir", project.getBasedir().getAbsolutePath());
      vars.put("project.basedir", project.getBasedir().getAbsolutePath());
      vars.put("project.build.directory", project.getBuild().getDirectory());
      vars.put("project.build.outputDirectory", project.getBuild().getOutputDirectory());
      vars.put("project.build.sourceDirectory", project.getBuild().getSourceDirectory());
      vars.put("project.build.scriptSourceDirectory", project.getBuild().getScriptSourceDirectory());
      vars.put("project.build.finalName", project.getBuild().getFinalName());
      
      for (String key : project.getProperties().stringPropertyNames()) {
        vars.put(key, project.getProperties().getProperty(key));
      }
      
      if (scriptProperties != null) {
        for (String key : scriptProperties.stringPropertyNames()) {
          vars.put(key, scriptProperties.getProperty(key));
        }
      }
      
      Interpreter interpreter = new Interpreter(new LogConsoleOutput(), connection);
      interpreter.interpret(new FileReader(scriptFile), vars);
      
    } catch (Throwable e) {
      throw new MojoExecutionException("Could not execute script " + scriptFile.getName(), e);
    } finally {
      Hub.shutdown();
    }
  }
  
  // -------------------------------------------------------------------------------------------
  
  private class LogConsoleOutput implements ConsoleOutput {
    
    StringBuffer buffer = new StringBuffer();
    
    @Override
    public void flush() {
    }
    
    @Override
    public void print(char c) {
      buffer.append(c);
    }
    
    @Override
    public void print(String s) {
      buffer.append(s);
    }
    
    @Override
    public void println() {
      buffer.append("");
      flushBuffer();
    }
    
    @Override
    public void println(String s) {
      buffer.append(s);
      flushBuffer();
    }
    
    private synchronized void flushBuffer() {
      if (buffer.length() > 0) {
        getLog().info(buffer.toString());
        buffer.delete(0, buffer.length());
      }
    }
  }
}
