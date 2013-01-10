package org.sapia.corus.client.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.Command;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.Console;
import org.sapia.console.ConsoleInput;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.InputException;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.corus.client.facade.CorusConnector;

/**
 * This class implements a {@link Console} that may be embedded in applications
 * in order to interpret commands.
 * 
 * @author yduchesne
 *
 */
public class Interpreter extends Console {
  
  private static final String COMMENT_MARKER = "#";
  
  private CorusCommandFactory commandFactory = new CorusCommandFactory();
  private CorusConnector      corus;
  private ClientFileSystem    fileSys;
  
  /**
   * Creates an instance of this class that sends command output to the console.
   * 
   * @param corus the {@link CorusConnector} to use.
   * @param fileSys the {@link ClientFileSystem} to use.
   */
  public Interpreter(CorusConnector corus, ClientFileSystem fileSys) {
    this(DefaultConsoleOutput.newInstance(), corus, fileSys);
  }
  
  /**
   * @param output the {@link ConsoleOutput} to which command output will be sent.
   * @param corus the {@link CorusConnector} to use.
   * @param fileSys the {@link ClientFileSystem} to use.
   */
  public Interpreter(ConsoleOutput output, CorusConnector corus, ClientFileSystem fileSys) {
    super(new InterpreterConsoleInput(), output);
    this.corus = corus;
    this.fileSys = fileSys;
  }
  
  /**
   * This method interprets the given command-line. That is: it parses it and processes
   * it into a command - executing the said command.
   * 
   * This method closes the reader provided as input upon exiting.
   * 
   * @param reader a {@link Reader} to read commands from.
   * @param the {@link Map} of variables to use when performing variable substitution.
   * 
   * @throws IOException if a problem occurs while trying to read commands from the given reader.
   * @throws CommandNotFoundException if the command on the command-line is unknown.
   * @throws InputException if some command arguments/options are missing/invalid.
   * @throws AbortException if execution of the command has been aborted.
   * @throws Throwable if an undefined error occurs.
   */  
  public void interpret(Reader reader, Map<String, String>  vars) throws IOException, CommandNotFoundException, InputException, AbortException, Throwable {

    Level old = Logger.getRootLogger().getLevel();
    Logger.getRootLogger().setLevel(Level.OFF);
    
    try {
      BufferedReader bufReader = new BufferedReader(reader);
      StrSubstitutor subs = new StrSubstitutor(new CompositeLookup().add(StrLookup.mapLookup(vars)).add(StrLookup.systemPropertiesLookup()));
      String commandLine = null;
      while ((commandLine = bufReader.readLine()) != null) {
        commandLine = subs.replace(commandLine).trim();
        if (!commandLine.isEmpty() && !commandLine.startsWith(COMMENT_MARKER)) {
          interpret(commandLine);
        }
      }
    } finally {
      Logger.getRootLogger().setLevel(old);
      try {
        reader.close();
      } catch (IOException e) {
        //noop
      }
    }
  }
  
  /**
   * This method interprets the given command-line. That is: it parses it and processes
   * it into a command - executing the said command.
   * 
   * @param commandLine the command-line to interpret.
   * @throws CommandNotFoundException if the command on the command-line is unknown.
   * @throws InputException if some command arguments/options are missing/invalid.
   * @throws AbortException if execution of the command has been aborted.
   * @throws Throwable if an undefined error occurs.
   */
  public void interpret(String commandLine) throws CommandNotFoundException, InputException, AbortException, Throwable  {

    Level old = Logger.getRootLogger().getLevel();
    Logger.getRootLogger().setLevel(Level.OFF);
    
    try {
      CmdLine cmdLine = CmdLine.parse(commandLine);
      if (cmdLine.isNextArg()) {
        Command cmd = commandFactory.getCommandFor(cmdLine.chopArg().getName());
        CliContextImpl ctx = new CliContextImpl(corus, new AutoFlushedBoundedList<CliError>(10), fileSys);
        ctx.setUp(this, cmdLine);
        ctx.setAbortOnError(true);
        try {
          cmd.execute(ctx);
        } catch (AbortException e) {
          if (!ctx.getErrors().isEmpty()) {
            throw ctx.getErrors().get(0).getCause();
          } else {
            throw e;
          }
        }
      } else {
        throw new IllegalArgumentException("Command expected (got empty command-line)");
      }
    } finally {
      Logger.getRootLogger().setLevel(old);
    }
      
  }
  
  // ==========================================================================
  
  static class InterpreterConsoleInput implements ConsoleInput {
    
    @Override
    public String readLine() throws IOException {
      throw new IllegalStateException("Cannot read command from input in interpreter mode");
    }
    
    @Override
    public char[] readPassword() throws IOException {
      throw new IllegalStateException("Cannot read password from input in interpreter mode");
    }
  }
  
  // ---------------------------------------------------------------------------
  
  static class CompositeLookup extends StrLookup {
    
    private List<StrLookup> lookups = new ArrayList<StrLookup>();
    
    public CompositeLookup add(StrLookup lookup) {
      lookups.add(lookup);
      return this;
    }
    
    @Override
    public String lookup(String name) {
      for (StrLookup lookup : lookups) {
        String value = lookup.lookup(name);
        if (value != null) {
          return value;
        }
      }
      return null;
    }
  }
}
