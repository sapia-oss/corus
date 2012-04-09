package org.sapia.corus.client.cli;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import org.sapia.console.CmdLine;
import org.sapia.console.CommandConsole;
import org.sapia.console.Console;
import org.sapia.console.ConsoleInputFactory;
import org.sapia.console.ConsoleListener;
import org.sapia.console.ConsoleOutput;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Localhost;


/**
 * This class is the entry point into the Corus command-line interface.
 * 
 * 
 * @author Yanick Duchesne
 */
public class CorusCli extends CommandConsole {
	
  public static final int    DEFAULT_PORT = 33000;
  public static final String HOST_OPT = "h";
  public static final String PORT_OPT = "p";
  public static final int    MAX_ERROR_HISTORY = 20;
  
  protected CorusConnector corus;
  private List<CliError> 	 errors;

  public CorusCli(CorusConnector corus) throws IOException {
    super(ConsoleInputFactory.createJLineConsoleInput(), ConsoleOutput.DefaultConsoleOutput.newInstance(), new CorusCommandFactory());
    super.setCommandListener(new CliConsoleListener());
    this.corus = corus;
    errors = new AutoFlushedBoundedList<CliError>(MAX_ERROR_HISTORY);
    
    // Change the prompt
    StringBuffer prompt = new StringBuffer().append("[");
    if (corus.getContext().getAddress().getTransportType().equals("tcp/socket")) {
      prompt.append(((TCPAddress) corus.getContext().getAddress()).getHost()).append(":").
             append(((TCPAddress) corus.getContext().getAddress()).getPort());
    } else {
      prompt.append(corus.getContext().getAddress().toString());
    }
    prompt.append("@").append(corus.getContext().getDomain()).append("]>> ");

    setPrompt(prompt.toString());
  }

  public static void main(String[] args) {
    String host = null;
    try{
      host = Localhost.getAnyLocalAddress().getHostAddress();
    }catch(UnknownHostException e){}
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
        CorusConnectionContext connection = new CorusConnectionContext(host, port);
        CorusConnector connector = new CorusConnector(connection);
        CorusCli    cli = new CorusCli(connector);
        
        try{
          cli.start();
        }catch(NullPointerException e){}
      }
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Exception e) {
      e.printStackTrace();
      if(e instanceof ConnectionException){
        System.out.println("No server listening at " + host + ":" + port);
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
    return new CliContextImpl(corus, errors);
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
      cons.println("");
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
