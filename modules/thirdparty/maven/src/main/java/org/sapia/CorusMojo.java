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
import org.sapia.corus.client.cli.InterpreterConsole;
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

  private static final String CRLF = System.getProperty("line.separator");
  
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
   * @required
   */
  private int port;
  
  /**
   * Location of the script file to execute.
   * 
   * @parameter expression="${scriptFile}"
   * @required
   */
  private File scriptFile;
  
  /**
   * @param default-value="${project}"
   * @required
   */
  private MavenProject project;
  
  /**
   * Optional properties to pass as to the script.
   * 
   * @parameter
   */
  private Properties scriptProperties;
  
  public void execute() throws MojoExecutionException {
    if (!scriptFile.exists()) {
      throw new MojoExecutionException("scriptFile not specified");
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
      vars.put("project.build.directory", project.getBuild().getDirectory());
      vars.put("project.build.outputDirectory", project.getBuild().getOutputDirectory());
      vars.put("project.build.sourceDirectory", project.getBuild().getSourceDirectory());
      vars.put("project.build.scriptSourceDirectory", project.getBuild().getScriptSourceDirectory());
      vars.put("project.build.finalName", project.getBuild().getFinalName());
      
      if (scriptProperties != null) {
        for (String key : scriptProperties.stringPropertyNames()) {
          vars.put(key, vars.get(key));
        }
      }
      
      InterpreterConsole console = new InterpreterConsole(new LogConsoleOutput(), connection);
      console.interpret(new FileReader(scriptFile), vars);
      
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
      buffer.append(CRLF);
      flushBuffer();
    }
    
    @Override
    public void println(String s) {
      buffer.append(s).append(CRLF);
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
