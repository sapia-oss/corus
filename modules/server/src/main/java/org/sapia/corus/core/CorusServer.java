package org.sapia.corus.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.text.StrLookup;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;
import org.apache.log.output.io.rotate.RotateStrategyByTime;
import org.apache.log.output.io.rotate.RotatingFileTarget;
import org.apache.log4j.Level;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.log.CompositeTarget;
import org.sapia.corus.log.FormatterFactory;
import org.sapia.corus.log.StdoutTarget;
import org.sapia.corus.log.SyslogTarget;
import org.sapia.corus.util.IOUtils;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketAddress;
import org.sapia.ubik.util.Localhost;

/**
 * This class is the entry point called from the 'java' command line.
 * 
 * @author Yanick Duchesne
 */
public class CorusServer {
  public static final int     DEFAULT_PORT         = 33000;
  public static final String  CONFIG_FILE_OPT      = "c";
  public static final String  PORT_OPT             = "p";
  public static final String  DOMAIN_OPT           = "d";
  public static final String  BIND_ADDR_OPT        = "a";
  public static final String  DEBUG_VERBOSITY      = "v";
  public static final String  DEBUG_FILE           = "f";
  public static final String  HELP            		 = "help";
  public static final String  PROP_SYSLOG_HOST     = "corus.server.syslog.host";
  public static final String  PROP_SYSLOG_PORT     = "corus.server.syslog.port";
  public static final String  PROP_SYSLOG_PROTOCOL = "corus.server.syslog.protocol";
  public static final String  PROP_INCLUDES        = "corus.server.properties.include";
  private static final String LOCK_FILE_NAME 			 = ".lock";
  
  @SuppressWarnings({ "deprecation" })
  public static void main(String[] args) {
    try {
    	
    	org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
      
      String corusHome = System.getProperty("corus.home");
      
      if (corusHome == null) {
        throw new CorusException("corus.home system property not set", ExceptionCode.INTERNAL_ERROR.getFullCode());
      } else {
        // hack to avoid headaches with backslashes in Properties.load()
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
      
      // ----------------------------------------------------------------------
      // Determining location of server properties 
      // (can be specified at command-line)
      
      String configFileName = "corus.properties";
      if (cmd.containsOption(CONFIG_FILE_OPT , true)) {
        configFileName = cmd.assertOption(CONFIG_FILE_OPT, true).getValue();
      }
      
      String aFilename = new StringBuffer(corusHome).
        append(File.separator).append("config").
        append(File.separator).append(configFileName).
        toString();      
      
      File propFile = new File(aFilename);

      // ----------------------------------------------------------------------
      // First off, we're loading the server properties to extract the includes
      
      Properties rawProps       = new Properties();
      PropertiesUtil.loadIfExist(rawProps, propFile);
      Properties includes       = PropertiesUtil.filter(
      		                          rawProps, 
      		                          PropertiesFilter.NamePrefixPropertiesFilter.createInstance(PROP_INCLUDES)
      		                        );
      
      includes = PropertiesUtil.replaceVars(includes, PropertiesStrLookup.systemPropertiesLookup());
      final Properties includedProps  = new Properties();
      Collection<String> includePaths = PropertiesUtil.values(includes);
      for(String includePath: includePaths) {
      	StringTokenizer stok = new StringTokenizer(includePath, ";:");
      	while(stok.hasMoreTokens()) {
      		String includePropFileName = stok.nextToken();
      		PropertiesUtil.loadIfExist(includedProps, new File(includePropFileName));
      		PropertiesUtil.replaceVars(includedProps, new StrLookup() {
						@Override
						public String lookup(String name) {
							String value = includedProps.getProperty(name);
							if(value == null) value = System.getProperty(name);
							return value;
						}
					});
      	}
      }
      
      //-----------------------------------------------------------------------
      // Now we're loading the server properties with the System properties and
      // included properties as parents (in that order). We're performing 
      // variable substitution.
      
      Properties corusProps = new Properties(System.getProperties());
      // copying the included props to the server props (server props will 
      // override included props
      PropertiesUtil.copy(includedProps, corusProps);
      if(propFile.exists()){
        InputStream is = null;
        InputStream propStream = null;
        try{
          propStream = IOUtils.replaceVars(new PropertiesStrLookup(corusProps), is = new FileInputStream(propFile));
          corusProps.load(propStream);
        }finally{
          if(is != null){
            try{ is.close(); }catch(Exception e){}
          }
        }
      }
      
      // ----------------------------------------------------------------------
      // Determining domain: can be specified at command line, or in server 
      // properties.
      
      String domain = null;
      if(cmd.containsOption(DOMAIN_OPT, true)){
        domain = cmd.assertOption(DOMAIN_OPT, true).getValue();
      } else if(corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN) != null){
        domain = corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN);
      } else{
        throw new CorusException("Domain must be set; pass -d option to command-line, "
           + " or configure " + CorusConsts.PROPERTY_CORUS_DOMAIN + " as part of "
           + " corus.properties", ExceptionCode.INTERNAL_ERROR.getFullCode());
      }
      
      System.setProperty(CorusConsts.PROPERTY_CORUS_DOMAIN, domain);
      
      // ----------------------------------------------------------------------
      // Determining port.
      
      if (cmd.containsOption(PORT_OPT, true)) {
        port = cmd.assertOption(PORT_OPT, true).asInt();
      }
      else if(corusProps.getProperty(CorusConsts.PROPERTY_CORUS_PORT) != null){
        port = Integer.parseInt(corusProps.getProperty(CorusConsts.PROPERTY_CORUS_PORT));
      }
      
      // ----------------------------------------------------------------------
      // Setting up logging.
      
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
      
      if (cmd.containsOption(DEBUG_FILE, false)) {

        File logsDir;
        
        if (cmd.containsOption(DEBUG_FILE, true)) {
          logsDir = new File(cmd.assertOption(DEBUG_FILE, true).getValue());
        } else {
          logsDir = new File(corusHome + File.separator + "logs");
        }
        logsDir.mkdirs();
        
        if (!logsDir.exists()) {
          throw new IOException("Log directory does not exist and could not be created: " + logsDir.getAbsolutePath());
        }
        
        Formatter            formatter = FormatterFactory.createDefaultFormatter();
        RotateStrategyByTime strategy  = new RotateStrategyByTime(1000 * 60 * 60 * 24);
        
        File logFile = new File(logsDir.getAbsolutePath() + File.separator + domain + "_" + port + ".log");
        
        RevolvingFileStrategy fileStrategy = new RevolvingFileStrategy(logFile, 5);
        RotatingFileTarget target = new RotatingFileTarget(formatter, strategy, fileStrategy);
        if (logTarget == null) {
          logTarget = new CompositeTarget();
        }
        logTarget.addTarget(target);
        
      } else {
        Formatter formatter = FormatterFactory.createDefaultFormatter();
        StdoutTarget target = new StdoutTarget(formatter);
        if (logTarget == null) {
          logTarget = new CompositeTarget();
        }
        logTarget.addTarget(target);
      }
      
      String syslogHost  = corusProps.getProperty(PROP_SYSLOG_HOST); 
      String syslogPort  = corusProps.getProperty(PROP_SYSLOG_PORT);
      String syslogProto = corusProps.getProperty(PROP_SYSLOG_PROTOCOL);
      
      if (syslogHost != null && syslogPort != null && syslogProto != null) {
        SyslogTarget target = new SyslogTarget(syslogProto, syslogHost, Integer.parseInt(syslogPort));
        logTarget.addTarget(target);
      }
      
      h.setDefaultLogTarget(logTarget);
      
      Logger serverLog = h.getLoggerFor(CorusServer.class.getName());
      if(serverLog.isDebugEnabled()) {
      	serverLog.debug("------------------------ Starting server with following properties: ------------------------");
      	Map<String, String> sortedServerProps = PropertiesUtil.map(corusProps);
      	for(Map.Entry<String, String> prop : sortedServerProps.entrySet()){
      		serverLog.debug(String.format("%s=%s", prop.getKey(), prop.getValue()));
      	}
      	serverLog.debug("--------------------------------------------------------------------------------------------");
      }
      
      // ----------------------------------------------------------------------
      // Exporting server
      
      if(cmd.containsOption(BIND_ADDR_OPT, true)){
        String pattern = cmd.assertOption(BIND_ADDR_OPT, true).getValue();
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      }      
      else if(corusProps.getProperty(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN) != null){
        String pattern = corusProps.getProperty(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN);
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      }
      
      String host = Localhost.getAnyLocalAddress().getHostAddress();

      CorusTransport aTransport = new TcpCorusTransport(host, port);
      
      // Create Lock file
      File lockFile = new File(corusHome + File.separator + "bin" + File.separator + LOCK_FILE_NAME + "_" + domain + "_" + port);
      IOUtils.createLockFile(lockFile);
   
      // Initialize Corus, export it and start it
      CorusImpl corus = new CorusImpl(new FileInputStream(aFilename), domain, new MultiplexSocketAddress(host, port), aTransport, corusHome);
      ServerContext context =  corus.getServerContext();
      
      // keeping reference to stub
      @SuppressWarnings("unused")
      Corus stub = (Corus) aTransport.exportObject(corus);
      corus.start();
      
      TCPAddress addr = ((TCPAddress) aTransport.getServerAddress());
      
      corus.setServerAddress(addr);
      
      System.out.println("Corus server ("+CorusVersion.create()+") started on: " + addr + ":" + port +
        ", domain: " + domain);
      
      EventDispatcher dispatcher = context.lookup(EventDispatcher.class);
      dispatcher.dispatch(new ServerStartedEvent(aTransport.getServerAddress()));
            
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      
      while (true) {
        Thread.sleep(100000);
      }
      
    } catch (InputException e) {
      System.out.println(e.getMessage());
      help();
    } catch (InterruptedException e) {
    	System.out.println("Interrupted, exiting.");
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  static final void help() {
    System.out.println();
    System.out.println("Corus server command-line syntax:");
    System.out.println();
    System.out.println("corus [-c filename] [-d domain] [-p port] [-v DEBUG|INFO|WARN|ERROR] [-f [path_to_log_dir]]");
    System.out.println();
    System.out.println("where:");
    System.out.println("  -c      specifies the corus configuration file to use (in the CORUS_HOME/config directory).");
    System.out.println("          If not specified the default file corus.properties will be used.");
    System.out.println();
    System.out.println("  -d      specifies the name of the domain to which the");
    System.out.println("          corus server should be associated.");
    System.out.println();
    System.out.println("  -p      specifies the port on which the corus server");
    System.out.println("          should run (defaults to 33000).");
    System.out.println();
    System.out.println("  -v      specifies the logging verbosity (defaults to DEBUG).");
    System.out.println();
    System.out.println("  -f      specifies if logging should be done to a file.");
    System.out.println("          If no option value is specified, the file will be:");
    System.out.println("          CORUS_HOME/logs/<domain>_<port>.log. If a value is specified");
    System.out.println("          it will be interpreted as the directory in which logs should");
    System.out.println("          be generated, that is, the Corus log will be:");
    System.out.println("          ${f_option_value}/<domain>_<port>.log.");
    System.out.println();
//    System.out.println("  -http   specifies the usage of the http transport provider.");
//    System.out.println("          If not present, the default tcp transport provider is used.");
//    System.out.println();
  }

}
