package org.sapia.corus;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.admin.Corus;
import org.sapia.corus.admin.CorusVersion;
import org.sapia.corus.taskmanager.CorusTaskManager;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Localhost;


/**
 * @author Yanick Duchesne
 */
public class CorusMonitor {
  public static final String HOST = "help";
  
  public static void main(String[] args) {
    int     port = CorusServer.DEFAULT_PORT;
    String  host;
    
    CmdLine cmd = CmdLine.parse(args);
    
    if(cmd.containsOption(HOST, false)){
      try {
        host = cmd.assertOption(HOST, true).getValue();
      } catch (InputException e) {
        System.out.println("Host (-h) not specified");
        help();
        
        return;
      }
    }
    else{
      try{
        host = Localhost.getLocalAddress().getHostAddress();
      }catch(UnknownHostException e){
        e.printStackTrace();
        return;
      }
    }
    
    CmdLine argsCmd = cmd.filterArgs();
    
    if (argsCmd.hasNext() &&
      ((Arg) argsCmd.next()).getName().equals(CorusServer.HELP)) {
      help();
      
      return;
    }
    
    int level = ProgressMsg.DEBUG;
    
    try {
      String verbosity = cmd.assertOption(CorusServer.DEBUG_VERBOSITY, true)
      .getValue();
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
    
    Corus            corus;
    CorusTaskManager taskman;
    
    try {
      corus     = (Corus) Hub.connect(host, port);
      taskman   = (CorusTaskManager) corus.lookup(CorusTaskManager.ROLE);
    } catch (Exception e) {
      e.printStackTrace();
      
      return;
    }
    
    ProgressQueue       queue = taskman.getProgressQueue(level);
    List<ProgressMsg>   msgs;
    ProgressMsg         msg;
    display();
    System.out.println("Waiting for Corus server output...");
    System.out.println();
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(queue));
    
    StringBuffer buf = new StringBuffer();
    
    try{
      while (!queue.isClosed()) {
        msgs = queue.fetchNext();
        
        for (int i = 0; i < msgs.size(); i++) {
          buf.delete(0, buf.length());
          msg = (ProgressMsg) msgs.get(i);
          buf.append("[").append(ProgressMsg.getLabelFor(msg.getStatus())).append("]");
          
          if (msg.isError()) {
            Throwable err = msg.getError();
            buf.append(err.getMessage());
            System.out.println(buf.toString());
            err.printStackTrace(System.out);
          } else {
            buf.append(msg.getMessage().toString());
            System.out.println(buf.toString());
          }
        }
      }
    }catch(UndeclaredThrowableException e){
      // we interpret this as being thrown by the progress queue
      // stub because the Corus server has been shut down.
    }
    System.out.println();
    System.out.println("Exiting; Corus server probably shut down.");
    
    try {
      Hub.shutdown(5000);
    } catch (Exception e) {
      //noop
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
    center("(c)2003-"+year+" sapia-oss.org");
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
