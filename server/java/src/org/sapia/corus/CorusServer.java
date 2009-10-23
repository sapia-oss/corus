package org.sapia.corus;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;
import org.apache.log.output.io.rotate.RotateStrategyByTime;
import org.apache.log.output.io.rotate.RotatingFileTarget;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.admin.CorusVersion;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.log.CompositeTarget;
import org.sapia.corus.log.FormatterFactory;
import org.sapia.corus.log.StdoutTarget;
import org.sapia.corus.log.SyslogTarget;
import org.sapia.soto.util.Utils;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Localhost;
import org.sapia.util.text.SystemContext;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusServer {
  public static final int    DEFAULT_PORT    = 33000;
  public static final String PORT_OPT        = "p";
  public static final String DOMAIN_OPT      = "d";
  public static final String BIND_ADDR_OPT   = "a";
  public static final String DEBUG_VERBOSITY = "v";
  public static final String FILE            = "f";
  public static final String HELP            = "help";
  public static final String PROP_SYSLOG_HOST     = "corus.server.syslog.host";
  public static final String PROP_SYSLOG_PORT     = "corus.server.syslog.port";
  public static final String PROP_SYSLOG_PROTOCOL = "corus.server.syslog.protocol";
  
  public static void main(String[] args) {
    //System.setProperty(Consts.LOG_LEVEL, "debug");
    try {
      
      String corusHome = System.getProperty("corus.home");
      
      if (corusHome == null) {
        throw new CorusException("corus.home system property not set");
      } else {
        // hack to avoid headackes with backslashes in Properties.load()
        corusHome = corusHome.replace('\\', '/');
        corusHome = corusHome.replace("\"", "");
        System.setProperty("corus.home", corusHome);
      }
      
      int     port = DEFAULT_PORT;
      
      CmdLine cmd;
      if(args.length == 0){
        cmd = new CmdLine();
      }
      else{
        cmd = CmdLine.parse(args);
      }
      
      CmdLine argsCmd = cmd.filterArgs();
      
      if (argsCmd.hasNext() && ((Arg) argsCmd.next()).getName().equals(HELP)) {
        help();
        
        return;
      }
      
      String aFilename = new StringBuffer(corusHome).
        append(File.separator).append("config").
        append(File.separator).append("corus.properties").
        toString();      
      
      File propFile = new File(aFilename);
      
      Properties corusProps = new Properties();
      if(propFile.exists()){
        InputStream is = null;
        InputStream propStream = null;
        try{
          propStream = Utils.replaceVars(new SystemContext(), is = new FileInputStream(propFile), aFilename);
          corusProps.load(propStream);
        }finally{
          if(is != null){
            try{ is.close(); }catch(Exception e){}
          }
        }
      }
      
      String domain = null;
      if(cmd.containsOption(DOMAIN_OPT, true)){
        domain = cmd.assertOption(DOMAIN_OPT, true).getValue();
      }
      else if(corusProps.getProperty(Consts.PROPERTY_CORUS_DOMAIN) != null){
        domain = corusProps.getProperty(Consts.PROPERTY_CORUS_DOMAIN);
      }
      else{
        throw new CorusException("Domain must be set; pass -d option to command-line, "
           + " or configure " + Consts.PROPERTY_CORUS_DOMAIN + " as part of "
           + " corus.properties");
      }
      
      System.setProperty(Consts.PROPERTY_CORUS_DOMAIN, domain);
      
      if (cmd.containsOption(PORT_OPT, true)) {
        port = cmd.assertOption(PORT_OPT, true).asInt();
      }
      else if(corusProps.getProperty(Consts.PROPERTY_CORUS_PORT) != null){
        port = Integer.parseInt(corusProps.getProperty(Consts.PROPERTY_CORUS_PORT));
      }
      
      
      /////////////// setting up logging //////////////////
      
      Hierarchy h = Hierarchy.getDefaultHierarchy();
      CompositeTarget logTarget = null;
      
      Priority  p = Priority.DEBUG;
      
      if (cmd.containsOption(DEBUG_VERBOSITY, true)) {
        p = Priority.getPriorityForName(cmd.assertOption(DEBUG_VERBOSITY, true)
        .getValue());
        
        if (p == null) {
          p = Priority.DEBUG;
        }
      }
      
      h.setDefaultPriority(p);
      
      if (cmd.containsOption(FILE, false)) {
        Formatter     formatter = FormatterFactory.createDefaultFormatter();
        RotateStrategyByTime strategy = new RotateStrategyByTime(1000 * 60 * 60 * 24);
        File                 logsDir  = new File(corusHome + File.separator +
          "logs");
        logsDir.mkdirs();
        
        File logFile = new File(logsDir.getAbsolutePath() + File.separator +
          domain + "_" + port + ".log");
        
        RevolvingFileStrategy fileStrategy = new RevolvingFileStrategy(logFile,
          5);
        RotatingFileTarget target = new RotatingFileTarget(formatter, strategy,
          fileStrategy);
        if(logTarget == null){
          logTarget = new CompositeTarget();
        }
        logTarget.addTarget(target);
      }
      else{
        Formatter formatter = FormatterFactory.createDefaultFormatter();
        StdoutTarget target = new StdoutTarget(formatter);
        if(logTarget == null){
          logTarget = new CompositeTarget();
        }
        logTarget.addTarget(target);
      }
      
      String syslogHost = corusProps.getProperty(PROP_SYSLOG_HOST); 
      String syslogPort = corusProps.getProperty(PROP_SYSLOG_PORT);
      String syslogProto = corusProps.getProperty(PROP_SYSLOG_PROTOCOL);
      
      if(syslogHost != null && syslogPort != null && syslogProto != null){
        SyslogTarget target = new SyslogTarget(syslogProto, syslogHost, Integer.parseInt(syslogPort));
        if(logTarget == null){
          logTarget = new CompositeTarget();
        }
        logTarget.addTarget(target);
      }
      
      if(logTarget != null){
        h.setDefaultLogTarget(logTarget);
      }
      
      /////////////// exporting server //////////////////
      
      if(cmd.containsOption(BIND_ADDR_OPT, true)){
        String pattern = cmd.assertOption(BIND_ADDR_OPT, true).getValue();
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      }      
      else if(corusProps.getProperty(Consts.PROPERTY_CORUS_ADDRESS_PATTERN) != null){
        String pattern = corusProps.getProperty(Consts.PROPERTY_CORUS_ADDRESS_PATTERN);
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      }
      
      String host = Localhost.getLocalAddress().getHostAddress();

      CorusTransport aTransport = new TcpCorusTransport(host, port);
   
      // Initialize Corus, export it and start it
      ServerContext context = CorusImpl.init(h, new FileInputStream(aFilename), domain, aTransport, corusHome);
      aTransport.exportObject(CorusImpl.getInstance());
      CorusImpl.start();
      
      TCPAddress addr = ((TCPAddress) aTransport.getServerAddress());
      
      context.setServerAddress(addr);
      
      System.out.println("Corus server ("+CorusVersion.create()+") started on: " + addr + ":" + port +
        ", domain: " + domain);
      
      EventDispatcher dispatcher = context.lookup(EventDispatcher.class);
      dispatcher.dispatch(new ServerStartedEvent(aTransport.getServerAddress()));
      
      Runtime.getRuntime().addShutdownHook(new ShutdownHook(h));
      
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      
      while (true) {
        Thread.sleep(100000);
      }
      
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  static final void help() {
    System.out.println();
    System.out.println("Corus server command-line syntax:");
    System.out.println();
    System.out.println("corus -d domain [-p port] [-v DEBUG|INFO|WARN|ERROR] [-f] [-http]");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -d      specifies the name of the domain to which the");
    System.out.println("          corus server should be associated.");
    System.out.println();
    System.out.println("  -p      specifies the port on which the corus server");
    System.out.println("          should run (defaults to 33000).");
    System.out.println();
    System.out.println("  -v      specifies the logging verbosity (defaults to DEBUG).");
    System.out.println();
    System.out.println("  -f      specifies if logging should be done to a file");
    System.out.println("          (if specified, the file will be under:");
    System.out.println("           CORUS_HOME/logs/domain_port.log");
    System.out.println();
    System.out.println("  -http   specifies the usage of the http transport provider.");
    System.out.println("          If not present, the default tcp transport provider is used.");
    System.out.println();
  }
  
  static class ShutdownHook extends Thread{
    private Hierarchy _log;
    
    ShutdownHook(Hierarchy h){
      _log = h;
    }
    
    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      _log.getLoggerFor(getClass().getName()).debug("Terminating...");
      CorusImpl.shutdown();
      _log.getLoggerFor(getClass().getName()).debug("Terminated.");
    }
  }
}
