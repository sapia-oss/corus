package org.sapia.corus.client.cli;


import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Level;
import org.sapia.console.CmdElement;
import org.sapia.console.CmdLine;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.CorusConnectionContextImpl;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.ubik.util.Localhost;


/**
 * This class provides scripting functionality.
 * 
 * @author Yanick Duchesne
 */
public class CorusScript {
	
  private static final int    DEFAULT_PORT       = 33000;
  private static final String GROOVY             = "groovy";
  private static final String HOST_OPT           = "h";
  private static final String PORT_OPT           = "p";
  private static final String SCRIPT_OPT         = "s";
  private static final String SCRIPT_INCLUDES    = "i";
  private static final String CORUS_CLI_VAR_NAME = "coruscli";
  private static final String BASEDIR_VAR_NAME   = "basedir";
 
  private static ClientFileSystem FILE_SYSTEM = new DefaultClientFileSystem();  
  
  private ScriptEngineFacade  scriptEngine;
  private Interpreter         interpreter;
  
  public CorusScript(CorusConnectionContextImpl connection, String[] includes) throws IOException {
    this(connection, includes, GROOVY);
  }
  
  public CorusScript(Interpreter interpreter, String[] includes, String scriptEngineName) throws IOException {
    this.interpreter = interpreter;
    List<String> paths = new ArrayList<String>();
    for (String i : includes) {
      paths.add(i);
    }
    paths.add(interpreter.getCorus().getContext().getFileSystem().getBaseDir().getAbsolutePath());
    ScriptEngineManager factory = new ScriptEngineManager();
    
    if (scriptEngineName.equalsIgnoreCase(GROOVY)) {
      scriptEngine = new GroovyScriptEngineFacade(
          paths.toArray(new String[paths.size()])
      );
    } else {
      scriptEngine = new JdkScriptEngineFacade(
          factory.getEngineByName(scriptEngineName), 
          paths.toArray(new String[paths.size()])
      );
    }
    
    interpreter.setOut(new ConsoleOutput() {
      @Override
      public void flush() {
      }
      @Override
      public void print(char c) {
      }
      @Override
      public void print(String s) {
      }
      @Override
      public void println() {
      }
      @Override
      public void println(String s) {
      }
    });

  }
  
  public CorusScript(CorusConnectionContextImpl context, String[] includes, String scriptEngineName) throws IOException {
    this(new Interpreter(new CorusConnectorImpl(context)), includes, scriptEngineName);
  }

  /**
   * @param scriptPath the path of the Groovy script to execute.
   * @param bindings the {@link Map} of variables to passed to the script.
   * @throws ScriptException if a problem occurs executing the script.
   * @throws ResourceException if a problem occurs trying to load the given script.
   */
  public void runScript(String scriptPath, Map<String, Object> bindings) throws ScriptException {
    bindings.put(CORUS_CLI_VAR_NAME, interpreter);
    bindings.put(BASEDIR_VAR_NAME, interpreter.getCorus().getContext().getFileSystem().getBaseDir().getAbsolutePath());
    scriptEngine.eval(scriptPath, bindings);
  }

  public static void main(String[] args) {
  	
  	// disabling log4j output
  	org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
  	
    String host = null;
    int port = DEFAULT_PORT;
    
    CorusConnectionContextImpl connection = null;
    try {
      CmdLine cmd = CmdLine.parse(args);

      if(cmd.containsOption("ver", false)){
        System.out.println("Corus client version: " + CorusVersion.create());
        System.out.println("Java version: " + System.getProperty("java.version"));
      }
      else if(cmd.containsOption("help", false)){
        help();
      }
      else{
        if(cmd.containsOption(HOST_OPT, true)){
          host = cmd.assertOption(HOST_OPT, true).getValue();
          if(host.equalsIgnoreCase("localhost")){
            host = Localhost.getAnyLocalAddress().getHostAddress();
          }
        }
        else{
          host = Localhost.getAnyLocalAddress().getHostAddress();
        }
  
        if (cmd.containsOption(PORT_OPT, true)) {
          port = cmd.assertOption(PORT_OPT, true).asInt();
        }
        String[] includes;
        if (cmd.containsOption(SCRIPT_INCLUDES, true)) {
          includes = cmd.assertOption(SCRIPT_INCLUDES, true).getValue().split(File.pathSeparator);
        } else {
          includes = new String[]{};
        }
        connection = new CorusConnectionContextImpl(host, port, FILE_SYSTEM);
        String scriptPath = cmd.assertOption(SCRIPT_OPT, true).getValue();
        CorusScript script = new CorusScript(connection, includes);
        
        Map<String, Object> bindings = new HashMap<String, Object>();
        while (cmd.hasNext()) {
          CmdElement e = cmd.next();
          if (e instanceof Option) {
            Option opt = (Option) e;
            bindings.put(e.getName(), opt.getValue());
          }
        }  
        script.runScript(scriptPath, bindings);
      }
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Throwable e) {
      if(e instanceof ConnectionException || e instanceof RemoteException){
        System.out.println("No server listening at " + host + ":" + port);
        e.printStackTrace();
      }
      else{
        e.printStackTrace();
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private static void help() {
    System.out.println();
    System.out.println("Corus Script command-line syntax:");
    System.out.println();
    System.out.println("coruscript [-h <host>] [-p <port>] -s script_path");
    System.out.println("or");
    System.out.println("coruscript -ver");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -h    host of the corus server to which to connect");
    System.out.println("        (defaults to local address).");
    System.out.println();
    System.out.println("  -p    specifies the port on which the corus server");
    System.out.println("        listens (defaults to 33000).");
    System.out.println();
    System.out.println("  -ver  indicates that the Corus version is to be");
    System.out.println("        displayed in the terminal.");
    System.out.println();
    System.out.println("  -s    specifies the path to a script to execute");
    System.out.println();
    System.out.println("  -help displays this help.");
  }

  // ==========================================================================
  // Inner classes and interfaces 
  
  interface ScriptEngineFacade {  
    public Object eval(String scriptPath, Map<String, Object> bindings) throws ScriptException;
  } 
  
  // --------------------------------------------------------------------------
  
  class GroovyScriptEngineFacade implements ScriptEngineFacade {
    
    private GroovyScriptEngine delegate;
    
    public GroovyScriptEngineFacade(String[] roots) throws IOException {
      this.delegate = new GroovyScriptEngine(roots);
    }
    
    @Override
    public Object eval(String scriptPath, Map<String, Object> bindings)
        throws ScriptException {
      Binding binding = new Binding(bindings);
      try {
        return delegate.run(scriptPath, binding);
      } catch (Exception e) {
        throw new ScriptException(e);
      }
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  class JdkScriptEngineFacade implements ScriptEngineFacade {
    
    private ScriptEngine delegate;
    private File[] roots;
    
    public JdkScriptEngineFacade(ScriptEngine delegate, String[] roots) {
      this.delegate = delegate;
      this.roots = new File[roots.length];
      for (int i = 0; i < roots.length; i++) {
        this.roots[i] = new File(roots[i]);
      }
    }
    
    @Override
    public Object eval(String scriptPath, Map<String, Object> bindings)
        throws ScriptException {
      for (File r : roots) {
        File script = new File(r, scriptPath);
        if (script.exists()) {
          try {
            return delegate.eval(new FileReader(script), new SimpleBindings(bindings));
          } catch (IOException e) {
            throw new ScriptException(e);
          }
        }
      }
      throw new ScriptException("No script file found for: " + scriptPath);
    }
  }
}