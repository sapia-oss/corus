package org.sapia.corus.admin.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.CommandConsole;
import org.sapia.console.Console;
import org.sapia.console.ConsoleListener;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.ReflectCommandFactory;
import org.sapia.corus.admin.CorusFacade;
import org.sapia.corus.admin.CorusFacadeImpl;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
    try {
      CmdLine cmd = CmdLine.parse(args);

      //System.setProperty(Consts.LOG_LEVEL, "debug");      
      String host = cmd.assertOption(HOST_OPT, true).getValue();
      int    port = DEFAULT_PORT;

      if (cmd.containsOption(PORT_OPT, true)) {
        port = cmd.assertOption(PORT_OPT, true).asInt();
      }

      CorusFacade fac = new CorusFacadeImpl(host, port);

      CorusCli    cli = new CorusCli(fac);
      cli.start();
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Exception e) {
      e.printStackTrace();
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
    System.out.println("coruscli -h host -p port");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -h host of the corus server to which to connect.");
    System.out.println();
    System.out.println("  -p specifies the port on which the corus server");
    System.out.println("     listens (defaults to 33000).");
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
      center(cons, "");
      center(cons, "Corus Command Line Interface");
      center(cons, "");
      center(cons, "R E S T R I C T E D   A C C E S S");
      center(cons, "");
      center(cons, "Authorized Users Only");
      center(cons, "");
      center(cons, "(c)2003 sapia-oss.org");
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

      StringBuffer b = new StringBuffer();

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
