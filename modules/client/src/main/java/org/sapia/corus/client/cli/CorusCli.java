package org.sapia.corus.client.cli;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.sapia.console.CmdElement;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandConsole;
import org.sapia.console.Console;
import org.sapia.console.ConsoleInput;
import org.sapia.console.ConsoleInputFactory;
import org.sapia.console.ConsoleListener;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.CorusConnectionContextImpl;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.ubik.util.Localhost;


/**
 * This class is the entry point into the Corus command-line interface.
 * 
 * 
 * @author Yanick Duchesne
 */
public class CorusCli extends CommandConsole {
	
  public static final int    DEFAULT_PORT = 33000;
  public static final String HOST_OPT   = "h";
  public static final String PORT_OPT   = "p";
  public static final String SCRIPT_OPT = "s";
  public static final int    MAX_ERROR_HISTORY = 20;
  
  private static ClientFileSystem FILE_SYSTEM = new DefaultClientFileSystem();  
  
  protected CorusConnectorImpl corus;
  private List<CliError> 	 errors;
  private boolean          abortOnError;


  public CorusCli(ConsoleInput input, ConsoleOutput output, CorusConnectorImpl corus) throws IOException {
    super(input, ConsoleOutput.DefaultConsoleOutput.newInstance(), new CorusCommandFactory());
    this.corus = corus;
    super.setCommandListener(new CliConsoleListener());
    this.corus = corus;
    errors = new AutoFlushedBoundedList<CliError>(MAX_ERROR_HISTORY);
    
    // Change the prompt
    setPrompt(CliUtils.getPromptFor(corus.getContext()));
  }
  
  public CorusCli(CorusConnectorImpl corus) throws IOException {
    this(selectConsoleInput(), ConsoleOutput.DefaultConsoleOutput.newInstance(), corus);
  }
  
  private static ConsoleInput selectConsoleInput() {
    String osName = System.getProperty("os.name").toLowerCase();
    
    // note: a case of the os.name being set to Vista has been reported.
    if(osName.indexOf("win") >= 0 || osName.indexOf("vista") >= 0) {
      return ConsoleInputFactory.createJdk6ConsoleInput();
    } else {
      try {
        return ConsoleInputFactory.createJLineConsoleInput();
      } catch (IOException e) {
        return ConsoleInputFactory.createJdk6ConsoleInput();
      }
    }
  }

  public static void main(String[] args) {
  	
  	// disabling log4j output
  	org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
  	
    String host = null;
    int port = DEFAULT_PORT;

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
        CorusConnectionContextImpl connection = new CorusConnectionContextImpl(host, port, FILE_SYSTEM);
        CorusConnectorImpl connector = new CorusConnectorImpl(connection);
        
        if (cmd.containsOption(SCRIPT_OPT, false)) {
          String path = cmd.assertOption(SCRIPT_OPT, true).getValue();
         
          Reader input = null;
          try {    
            input = new FileReader(new File(path));
          } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
          }
          Map<String, String> vars = new HashMap<String, String>();
          for (int i = 0; i < cmd.size(); i++) {
            CmdElement elem = cmd.get(i);
            if (elem instanceof Option) {
              Option opt = (Option) elem;
              if (opt.getValue() != null) {
                vars.put(elem.getName(), ((Option) elem).getValue());
              }
            }
          }
          try {
            Interpreter console = new Interpreter(DefaultConsoleOutput.newInstance(), connector);
            console.interpret(input, vars);
            System.exit(0);
          } catch (Throwable err) {
            err.printStackTrace();
            System.exit(1);
          }
        } else {
         try{
           CorusCli cli = new CorusCli(connector);
            cli.start();
          }catch(NullPointerException e){
          	e.printStackTrace();
          }
        }
      }
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Exception e) {
      if(e instanceof ConnectionException || e instanceof RemoteException){
        System.out.println("No server listening at " + host + ":" + port);
        e.printStackTrace();
      }
      else{
        e.printStackTrace();
      }
    }
  }

  /*////////////////////////////////////////////////////////////////////
                               RESTRICTED METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.console.CommandConsole#newContext()
   */
  protected Context newContext() {
    CliContextImpl context = new CliContextImpl(corus, errors);
    context.setAbortOnError(abortOnError);
    return context;
  }

  private static void help() {
    System.out.println();
    System.out.println("Corus client command-line syntax:");
    System.out.println();
    System.out.println("coruscli [-h <host>] [-p <port>]");
    System.out.println("or");
    System.out.println("coruscli -ver");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -h    host of the corus server to which to connect");
    System.out.println("        (defaults to local address).");
    System.out.println();
    System.out.println("  -p    specifies the port on which the corus server");
    System.out.println("        listens (defaults to 33000).");
    System.out.println();
    System.out.println("  -ver  indicates that the version of this command-line");
    System.out.println("        is to be displayed in the terminal.");
    System.out.println();
    System.out.println("  -s    specifies the path to a script to execute");
    System.out.println();
    System.out.println("  -help displays this help.");
  }

  /*////////////////////////////////////////////////////////////////////
                                INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  public static class CliConsoleListener implements ConsoleListener {
    /**
     * @see org.sapia.console.ConsoleListener#onAbort(Console)
     */
    public void onAbort(Console cons) {
      cons.println("Good bye...");
      System.exit(0);
    }

    /**
     * @see org.sapia.console.ConsoleListener#onCommandNotFound(Console, String)
     */
    public void onCommandNotFound(Console cons, String command) {
      cons.println("Command not recognized: " + command);
    }

    /**
     * @see org.sapia.console.ConsoleListener#onStart(Console)
     */
    public void onStart(Console cons) {
      line(cons);
      
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      
      center(cons, "");
      center(cons, "Corus Command Line Interface (" + CorusVersion.create().toString() + ")");
      center(cons, "");
      center(cons, "R E S T R I C T E D   A C C E S S");
      center(cons, "");
      center(cons, "Authorized Users Only");
      center(cons, "");
      center(cons, "(c)2002-" + year + " sapia-oss.org");
      center(cons, "");
      line(cons);
      cons.println();
      
      cons.println("-> Type man to see the list of available commands.");
      cons.println("-> Type man <command_name> to see help about a specific command.");
      cons.println();
    }

    static void line(Console cons) {
      for (int i = 0; i < 80; i++) {
        cons.print("*");
      }

      cons.println("");
    }

    static void center(Console cons, String text) {
      int margin = (80 - text.length()) / 2;
      cons.print("*");

      for (int i = 0; i < (margin - 1); i++) {
        cons.print(" ");
      }

      cons.print(text);

      if ((text.length() % 2) == 0) {
        for (int i = 0; i < (margin - 1); i++) {
          cons.print(" ");
        }
      } else {
        for (int i = 0; i < margin; i++) {
          cons.print(" ");
        }
      }

      cons.println("*");
    }
  }
}
