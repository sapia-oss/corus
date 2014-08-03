package org.sapia.corus.client.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;

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
import org.sapia.console.TerminalFacade;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.console.InputException;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.FacadeInvocationContext;
import org.sapia.corus.client.sort.SortSwitchInfo;

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
  private CorusConnector corus;

  /**
   * Creates an instance of this class that sends command output to the console.
   * 
   * @param corus
   *          the {@link CorusConnector} to use.
   */
  public Interpreter(CorusConnector corus) {
    this(
        CorusConsoleOutput.DefaultCorusConsoleOutput.wrap(
            DefaultConsoleOutput.newInstance()
        ), 
        corus
    );
  }

  /**
   * @param output
   *          the {@link ConsoleOutput} to which command output will be sent.
   * @param corus
   *          the {@link CorusConnector} to use.
   */
  public Interpreter(ConsoleOutput output, CorusConnector corus) {
    super(new InterpreterConsoleInput(), output);
    this.corus = corus;
  }
  
  /**
   * @param parent
   *          the {@link Console} from which this instance is created.
   * @param corus
   *          the {@link CorusConnector} to use.
   */
  public Interpreter(Console parent, CorusConnector corus) {
    super(new InterpreterConsoleInput(), parent.out());
    this.setWidth(parent.getWidth());
    this.corus = corus;
  }

  /**
   * @return this instance's {@link CorusConnector}.
   */
  public CorusConnector getCorus() {
    return corus;
  }

  /**
   * This method interprets the given command-line. That is: it parses it and
   * processes it into a command - executing the said command.
   * 
   * This method closes the reader provided as input upon exiting.
   * 
   * @param reader
   *          a {@link Reader} to read commands from.
   * @param the
   *          {@link StrLookup} of variables to use when performing variable
   *          substitution.
   * 
   * @throws IOException
   *           if a problem occurs while trying to read commands from the given
   *           reader.
   * @throws CommandNotFoundException
   *           if the command on the command-line is unknown.
   * @throws InputException
   *           if some command arguments/options are missing/invalid.
   * @throws AbortException
   *           if execution of the command has been aborted.
   * @throws Throwable
   *           if an undefined error occurs.
   */
  public void interpret(Reader reader, StrLookup vars) throws IOException, CommandNotFoundException, InputException, AbortException, Throwable {

    Level old = Logger.getRootLogger().getLevel();
    disableLogging();
    try {
      BufferedReader bufReader = new BufferedReader(reader);
      StrSubstitutor subs = new StrSubstitutor(vars);
      String commandLine = null;
      while ((commandLine = bufReader.readLine()) != null) {
        commandLine = subs.replace(commandLine).trim();
        if (!commandLine.isEmpty() && !commandLine.startsWith(COMMENT_MARKER)) {
          eval(commandLine, vars);
        }
      }
    } finally {
      enableLogging(old);
      try {
        reader.close();
      } catch (IOException e) {
        // noop
      }
    }
  }

  /**
   * This method interprets the given command-line. That is: it parses it and
   * processes it into a command - executing the said command.
   * 
   * @param commandLine
   *          the command-line to interpret.
   * @param the
   *          {@link StrLookup} holding variables to use.
   * @throws CommandNotFoundException
   *           if the command on the command-line is unknown.
   * @throws InputException
   *           if some command arguments/options are missing/invalid.
   * @throws AbortException
   *           if execution of the command has been aborted.
   * @throws Throwable
   *           if an undefined error occurs.
   * @return this instance.
   */
  public Object eval(String commandLine, StrLookup vars) throws CommandNotFoundException, InputException, AbortException, Throwable {

    Level old = Logger.getRootLogger().getLevel();
    disableLogging();

    try {
      CmdLine cmdLine = CmdLine.parse(commandLine);
      if (cmdLine.isNextArg()) {
        Command cmd = commandFactory.getCommandFor(cmdLine.chopArg().getName());
        AtomicReference<SortSwitchInfo[]> switches = new AtomicReference<SortSwitchInfo[]>();
        switches.set(new SortSwitchInfo[]{});
        CliContextImpl ctx = new CliContextImpl(corus, new AutoFlushedBoundedList<CliError>(10), vars, switches);
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
      enableLogging(old);
    }
    return FacadeInvocationContext.get();
  }

  /**
   * Corus commands may return values that may then be acquired using this
   * method.
   * 
   * @return the return value (if any) of the last interpreted command-line.
   */
  public Object get() {
    return FacadeInvocationContext.get();
  }

  // ==========================================================================

  static class InterpreterConsoleInput implements ConsoleInput {
    
    private TerminalFacade terminal = new DefaultTerminalFacade();

    @Override
    public String readLine() throws IOException {
      throw new IllegalStateException("Cannot read command from input in interpreter mode");
    }
    
    @Override
    public TerminalFacade getTerminal() {
      return terminal;
    }

    @Override
    public char[] readPassword() throws IOException {
      throw new IllegalStateException("Cannot read password from input in interpreter mode");
    }
  }

  /**
   * Disables logging.
   */
  protected void disableLogging() {
    Logger.getRootLogger().setLevel(Level.OFF);
  }

  
  /**
   * @param level {@link Level} the level reassign.
   */
  protected void enableLogging(Level level) {
    Logger.getRootLogger().setLevel(level);
  }

}
