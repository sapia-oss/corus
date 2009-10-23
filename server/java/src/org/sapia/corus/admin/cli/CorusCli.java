package org.sapia.corus.admin.cli;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Calendar;

import org.sapia.console.CmdLine;
import org.sapia.console.CommandConsole;
import org.sapia.console.Console;
import org.sapia.console.ConsoleListener;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.ReflectCommandFactory;
import org.sapia.corus.admin.CorusFacade;
import org.sapia.corus.admin.CorusFacadeImpl;
import org.sapia.corus.admin.CorusVersion;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Localhost;


/**
 * This class is the entry point into the Corus command-line interface.
 * 
 * @author Yanick Duchesne
 */
public class CorusCli extends CommandConsole {
  public static final int    DEFAULT_PORT = 33000;
  public static final String HOST_OPT = "h";
  public static final String PORT_OPT = "p";
  protected CorusFacade     _corus;

  public CorusCli(CorusFacade corus) {
    super(new ReflectCommandFactory().addPackage("org.sapia.corus.admin.cli.command"));
    super.setCommandListener(new CliConsoleListener());
    _corus = corus;
    
    // Change the prompt
    StringBuffer prompt = new StringBuffer().append("[");
    if (_corus.getServerAddress().getTransportType().equals("tcp/socket")) {
      prompt.append(((TCPAddress) _corus.getServerAddress()).getHost()).append(":").
             append(((TCPAddress) _corus.getServerAddress()).getPort());
    } else {
      prompt.append(_corus.getServerAddress().toString());
    }
    prompt.append("@").append(_corus.getDomain()).append("]>> ");

    setPrompt(prompt.toString());
  }

  public static void main(String[] args) {
    String host = null;
    try{
      host = Localhost.getLocalAddress().getHostAddress();
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
            host = Localhost.getLocalAddress().getHostAddress();
          }
        }
        else{
          host = Localhost.getLocalAddress().getHostAddress();
        }
  
        if (cmd.containsOption(PORT_OPT, true)) {
          port = cmd.assertOption(PORT_OPT, true).asInt();
        }
  
        CorusFacade fac = new CorusFacadeImpl(host, port);
  
        CorusCli    cli = new CorusCli(fac);
        cli.start();
      }
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Exception e) {
      e.printStackTrace();
      if(e instanceof CorusException && 
         ((CorusException)e).getCause() != null && 
         ((CorusException)e).getCause() instanceof RemoteException){
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
    return new CliContext(_corus);
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
      center(cons, "(c)2003-" + year + " sapia-oss.org");
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
