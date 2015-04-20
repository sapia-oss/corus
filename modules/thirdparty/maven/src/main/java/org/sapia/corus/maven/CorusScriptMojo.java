package org.sapia.corus.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sapia.console.ConsoleOutput;
import org.sapia.corus.client.cli.CorusCli;
import org.sapia.corus.client.cli.DefaultClientFileSystem;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.facade.CorusConnectionContextImpl;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.ubik.rmi.server.Hub;

/**
 * Maven goal that allow to run Corus commands trough a script file or using inlined commands.
 */
@Mojo(name = "script", defaultPhase = LifecyclePhase.PACKAGE)
public class CorusScriptMojo extends BaseCorusMojoSupport {

  public static final String DEFAULT_HOST_VALUE = "127.0.0.1";
  public static final String DEFAULT_PORT_VALUE = "" + CorusCli.DEFAULT_PORT;

  /**
   * The host of the Corus server to which to connect.
   */
  @Parameter(property = "host", defaultValue = DEFAULT_HOST_VALUE)
  private String host = DEFAULT_HOST_VALUE;

  /**
   * The port of the Corus server to which to connect.
   */
  @Parameter(property = "port", defaultValue = DEFAULT_PORT_VALUE)
  private int port = CorusCli.DEFAULT_PORT ;

  /**
   * Location of the script file to execute.
   */
  @Parameter(property = "scriptFile")
  private File scriptFile;

  /**
   * Content of the corus script to execute (contains corus cli commands).
   */
  @Parameter(property = "script")
  private String script;

  /**
   * Location of the base directory from which the corus client comment will be executed.
   */
  @Parameter(property = "baseDir")
  private File baseDir;

  /**
   * The maven project that uses this Mojo
   */
  @Parameter(required = true, readonly = true, property = "project")
  private MavenProject mvnProject;

  /**
   * Optional properties to pass as to the script.
   */
  @Parameter(property = "scriptProperties")
  private Properties scriptProperties;

  /* (non-Javadoc)
   * @see org.sapia.corus.maven.CorusMojoSupport#doExecute()
   */
  @Override
  protected void doExecute() throws MojoExecutionException, MojoFailureException {
    // Validate script to execute
    if (scriptFile != null && script != null) {
      String message = "Cannot provide both 'scriptFile' and 'script' properties";
      getLog().error(message);
      throw new MojoExecutionException(message);

    } else if (scriptFile != null) {
      if (!scriptFile.exists() || !scriptFile.isFile()) {
        String message = "The specified 'scriptFile' is not a valid existing file: " + scriptFile.getAbsolutePath();
        getLog().error(message);
        throw new MojoExecutionException(message);
      } else if (scriptFile.canRead()) {
        String message = "The specified 'scriptFile' is not readable: " + scriptFile.getAbsolutePath();
        getLog().error(message);
        throw new MojoExecutionException(message);
      }

    } else if (script != null) {
      if (script.trim().isEmpty()) {
        String message = "The specified 'script' cannot be blank";
        getLog().error(message);
        throw new MojoExecutionException(message);
      }

    } else {
      String message = "No script to execute: make sure one of the 'scriptFile' or 'script' property is defined";
      getLog().error(message);
      throw new MojoExecutionException(message);
    }

    if (scriptFile != null && (!scriptFile.exists() || !scriptFile.isFile())) {
      String message = "The specified 'scriptFile' is not a valid existing file: " + scriptFile.getAbsolutePath();
      getLog().error(message);
      throw new MojoExecutionException(message);
    }

    // Validate baseDir
    if (baseDir == null) {
      baseDir = new File(System.getProperty("user.dir"));
    }
    if (!baseDir.exists() || !baseDir.isDirectory()) {
      String message = "The specified 'baseDir' is not a valid existing directory: " + scriptFile.getAbsolutePath();
      getLog().error(message);
      throw new MojoExecutionException(message);
    }

    // Performs connection to specified corus server
    CorusConnectionContextImpl corusContext = null;
    try {
      getLog().info("Connecting to Corus server " + host + ":" + port);
      corusContext = new CorusConnectionContextImpl(host, port, new DefaultClientFileSystem(baseDir));
    } catch (Exception e) {
      throw new MojoExecutionException(String.format("Could not connect to Corus server at %s:%s", host, port), e);
    }

    Reader corusCommandsReader = null;
    try {
      CorusConnectorImpl connection = new CorusConnectorImpl(corusContext);

      // Initialize properties for script execution
      Map<String, String> vars = new HashMap<String, String>();
      vars.put("project.groupId", mvnProject.getGroupId());
      vars.put("project.artifactId", mvnProject.getArtifactId());
      vars.put("project.description", mvnProject.getDescription());
      vars.put("project.id", mvnProject.getId());
      vars.put("project.inceptionYear", mvnProject.getInceptionYear());
      vars.put("project.name", mvnProject.getName());
      vars.put("project.version", mvnProject.getVersion());
      vars.put("project.baseDir", mvnProject.getBasedir().getAbsolutePath());
      vars.put("project.build.directory", mvnProject.getBuild().getDirectory());
      vars.put("project.build.outputDirectory", mvnProject.getBuild().getOutputDirectory());
      vars.put("project.build.sourceDirectory", mvnProject.getBuild().getSourceDirectory());
      vars.put("project.build.scriptSourceDirectory", mvnProject.getBuild().getScriptSourceDirectory());
      vars.put("project.build.finalName", mvnProject.getBuild().getFinalName());

      for (String key : mvnProject.getProperties().stringPropertyNames()) {
        vars.put(key, mvnProject.getProperties().getProperty(key));
      }

      if (scriptProperties != null) {
        for (String key : scriptProperties.stringPropertyNames()) {
          vars.put(key, scriptProperties.getProperty(key));
        }
      }

      // Pickup up the corus commands to execute
      if (scriptFile != null) {
        getLog().info("Executing script file: " + scriptFile.getAbsolutePath() + " ...");
        corusCommandsReader = new BufferedReader(new FileReader(scriptFile));
      } else {
        getLog().info("Executing inlined script ...");
        corusCommandsReader = new StringReader(script);
      }

      Interpreter interpreter = new Interpreter(new LogConsoleOutput(), connection);
      interpreter.interpret(corusCommandsReader, new CompositeStrLookup().add(StrLookup.mapLookup(vars)).add(StrLookup.systemPropertiesLookup()));

    } catch (Throwable e) {
      String message = "System error executing corus script";
      getLog().error(message);
      throw new MojoFailureException(message, e);

    } finally {
      if (corusCommandsReader != null) {
        try {
          corusCommandsReader.close();
        } catch (IOException e) {
        }
      }

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
