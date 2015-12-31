package org.sapia.corus.core;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.taskmanager.CorusTaskManager;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * @author Yanick Duchesne
 */
public class CorusMonitor {
  public static final String HOST = "h";
  public static final String HELP = "help";

  public static void main(String[] args) {
    int port = CorusServer.DEFAULT_PORT;
    String host;

    CmdLine cmd = CmdLine.parse(args);

    if (cmd.containsOption(HELP, false)) {
      help();
      return;
    } else if (cmd.containsOption(HOST, false)) {
      try {
        host = cmd.assertOption(HOST, true).getValue();
      } catch (InputException e) {
        System.out.println("Host (-h) not specified");
        help();

        return;
      }
    } else {
      host = "localhost";
    }

    int level = ProgressMsg.DEBUG;

    try {
      String verbosity = cmd.assertOption(CorusServer.LOG_VERBOSITY_OPT, true).getValue();
      verbosity = verbosity.toLowerCase();

      if (verbosity.startsWith("debug")) {
        level = ProgressMsg.DEBUG;
      } else if (verbosity.startsWith("info")) {
        level = ProgressMsg.INFO;
      } else if (verbosity.startsWith("warn")) {
        level = ProgressMsg.WARNING;
      } else if (verbosity.startsWith("error")) {
        level = ProgressMsg.ERROR;
      }
    } catch (InputException e) {
    }

    Corus corus;
    CorusTaskManager taskman;

    try {
      ServerAddress connectAddress;
      if (InetAddressUtils.isIPv4Address(host)) {
        connectAddress = HttpAddress.newDefaultInstance(host, port);
      } else if (InetAddressUtils.isIPv6Address(host)) {
        connectAddress = HttpAddress.newDefaultInstance(host, port);
      } else {
        try {
          InetAddress addr = InetAddress.getByName(host);
          connectAddress = HttpAddress.newDefaultInstance(addr.getHostAddress(), port);
        } catch (java.net.UnknownHostException e) {
          throw new IllegalArgumentException("Unkown host: " + host, e);
        }
      }
      corus = (Corus) Hub.connect(connectAddress);
      taskman = (CorusTaskManager) corus.lookup(CorusTaskManager.ROLE);
    } catch (Exception e) {
      e.printStackTrace();

      return;
    }

    ProgressQueue queue = taskman.getProgressQueue(level);
    List<ProgressMsg> msgs;
    ProgressMsg msg;
    display();
    System.out.println("Waiting for Corus server output...");
    System.out.println();
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(queue));

    StringBuffer buf = new StringBuffer();

    try {
      while (!queue.isClosed()) {
        msgs = queue.fetchNext();

        for (int i = 0; i < msgs.size(); i++) {
          buf.delete(0, buf.length());
          msg = (ProgressMsg) msgs.get(i);
          buf.append("[").append(ProgressMsg.getLabelFor(msg.getStatus())).append("]");

          if (msg.isError() && msg.isThrowable()) {
            Throwable err = msg.getThrowable();
            buf.append(err.getMessage());
            System.out.println(buf.toString());
            err.printStackTrace(System.out);
          } else {
            buf.append(msg.getMessage().toString());
            System.out.println(buf.toString());
          }
        }
      }
    } catch (UndeclaredThrowableException e) {
      // we interpret this as being thrown by the progress queue
      // stub because the Corus server has been shut down.
    }
    System.out.println();
    System.out.println("Exiting; Corus server probably shut down.");

    try {
      Hub.shutdown(5000);
    } catch (Exception e) {
      // noop
    }
  }

  static final void help() {
    System.out.println();
    System.out.println("Corus monitor command-line syntax:");
    System.out.println();
    System.out.println("corus -h host [-p port] [-v DEBUG|INFO|WARN|ERROR]");
    System.out.println();
    System.out.println("where:");
    System.out.println();
    System.out.println("  -h      specifies the host of the corus server to");
    System.out.println("          monitor.");
    System.out.println();
    System.out.println("  -p      specifies the port of the corus server to");
    System.out.println("          monitor (defaults to 33000).");
    System.out.println();
    System.out.println("  -v      specifies the minimal verbosity of messages");
    System.out.println("          that will be monitored.");
    System.out.println();
  }

  public static void display() {
    line();

    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);

    center("");
    center("Corus Monitor (" + CorusVersion.create().toString() + ")");
    center("");
    center("R E S T R I C T E D   A C C E S S");
    center("");
    center(" - Type CTRL-C to exit -");
    center("");
    center("Authorized Users Only");
    center("");
    center("(c)2003-" + year + " sapia-oss.org");
    center("");
    line();
    System.out.println("");
  }

  static void line() {
    for (int i = 0; i < 80; i++) {
      System.out.print("*");
    }

    System.out.println("");
  }

  static void center(String text) {
    int margin = (80 - text.length()) / 2;
    System.out.print("*");

    for (int i = 0; i < (margin - 1); i++) {
      System.out.print(" ");
    }

    System.out.print(text);

    if ((text.length() % 2) == 0) {
      for (int i = 0; i < (margin - 1); i++) {
        System.out.print(" ");
      }
    } else {
      for (int i = 0; i < margin; i++) {
        System.out.print(" ");
      }
    }

    System.out.println("*");
  }

  static class ShutdownHook extends Thread {
    private ProgressQueue _queue;

    ShutdownHook(ProgressQueue queue) {
      _queue = queue;
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      try {
        _queue.close();
      } catch (Exception e) {
        // noop
      }

      try {
        Hub.shutdown(5000);
      } catch (Exception e) {
        // noop
      }
    }
  }
}
