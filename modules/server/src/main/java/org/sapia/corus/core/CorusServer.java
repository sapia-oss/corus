package org.sapia.corus.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang.text.StrLookup;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;
import org.apache.log.output.io.rotate.RotateStrategyByTime;
import org.apache.log.output.io.rotate.RotatingFileTarget;
import org.apache.log4j.Level;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.audit.AuditLogFormatter;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.CliUtil;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.cloud.CorusUserData;
import org.sapia.corus.cloud.CorusUserDataEvent;
import org.sapia.corus.cloud.CorusUserDataFactory;
import org.sapia.corus.log.CompositeTarget;
import org.sapia.corus.log.FormatterFactory;
import org.sapia.corus.log.StdoutTarget;
import org.sapia.corus.log.SyslogTarget;
import org.sapia.corus.util.CorusTimestampOutputStream;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpConsts;
import org.sapia.ubik.util.Conf;

/**
 * This class is the entry point called from the <tt>java</tt> command line.
 * 
 * @author Yanick Duchesne
 */
public class CorusServer {
  
  public static final int DEFAULT_PORT            = 33000;
  public static final String CONFIG_FILE_OPT      = "c";
  public static final String PORT_OPT             = "p";
  public static final String DOMAIN_OPT           = "d";
  public static final String BIND_ADDR_OPT        = "a";
  public static final String LOG_VERBOSITY_OPT    = "v";
  public static final String LOG_FILE_OPT         = "f";
  public static final String USER_DATA            = "u";
  public static final String PROP_SYSLOG_HOST     = "corus.server.syslog.host";
  public static final String PROP_SYSLOG_PORT     = "corus.server.syslog.port";
  public static final String PROP_SYSLOG_PROTOCOL = "corus.server.syslog.protocol";
  public static final String PROP_INCLUDES        = "corus.server.properties.include";
  private static final String LOCK_FILE_NAME      = ".lock";
  
  /**
   * @param args
   */
  @SuppressWarnings({ "deprecation" })
  public static void main(String[] args) {
    
    System.setProperty(HttpConsts.HTTP_CLIENT_JDK, "true");
    
    System.setOut(new CorusTimestampOutputStream(System.out));
    System.setErr(new CorusTimestampOutputStream(System.err));
    
    try {

      org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
      
      if (System.getProperty("corus.home") == null) {
        String userDir = System.getProperty("user.dir");
        System.out.println("WARNING: corus.home system property not set. Will be set to " + userDir + ".");
        System.out.println("(In a proper Corus installation, corus.home is derived from the CORUS_HOME environment variable.");
        System.setProperty("corus.home", userDir);
      }

      String corusHome = FileUtil.fixFileSeparators(System.getProperty("corus.home"));
      
      // hack to avoid headaches with backslashes in Properties.load()
      String hackedCorusHome = corusHome.replace("\\", "\\\\");
      hackedCorusHome = hackedCorusHome.replace("\"", "");
      System.setProperty("corus.home", hackedCorusHome);

      CmdLine cmd;
      if (args.length == 0) {
        cmd = new CmdLine();
      } else {
        cmd = CmdLine.parse(args);
      }

      if (CliUtil.isHelp(cmd)) {
        help();
        return;
      }

      CorusUserData userData;
      Exception userDataFetchError = null;
      if (cmd.containsOption(USER_DATA, false)) {
        try {
          Option userDataOpt = cmd.assertOption(USER_DATA, false);
          if (userDataOpt.getValue() != null) {
            userData = CorusUserDataFactory.fetchUserData(URI.create(userDataOpt.getValue()));
          } else {
            userData = CorusUserDataFactory.fetchUserData();
          }
        } catch (Exception e) {
          userDataFetchError = e;
          userData = new CorusUserData();
        }
      } else {
        userData = new CorusUserData();
      }

      // ----------------------------------------------------------------------
      // If user-data specifies port, use it (has priority).

      int port = DEFAULT_PORT;
      if (userData.getServerProperties().containsKey(CorusConsts.PROPERTY_CORUS_PORT)) {
        port = Integer.parseInt(userData.getServerProperties().getProperty(CorusConsts.PROPERTY_CORUS_PORT));
      } else if (cmd.containsOption(PORT_OPT, true)) {
        port = cmd.assertOption(PORT_OPT, true).asInt();
      }

      // ----------------------------------------------------------------------
      // Setting up files for config loading
      
      String configFileName = "corus.properties";
      if (cmd.containsOption(CONFIG_FILE_OPT, true)) {
        configFileName = cmd.assertOption(CONFIG_FILE_OPT, true).getValue();
      }
      
      File propFile = FilePath.newInstance()
          .addDir(corusHome)
          .addDir("config")
          .setRelativeFile(configFileName).createFile();
      
      File specificPropFile = FilePath.newInstance()
          .addDir(corusHome)
          .addDir("config")
          .setRelativeFile("corus_" + port + ".properties")
          .createFile();

      // files under $HOME/.corus
      File userPropFile = FilePath.forCorusUserDir()
          .setRelativeFile("corus.properties")
          .createFile();

      File userSpecificPropFile = FilePath.forCorusUserDir()
          .setRelativeFile("corus_" + port + ".properties")
          .createFile();
      
      // ----------------------------------------------------------------------
      // First off, we're loading the server properties to extract the includes

      Properties rawProps = new Properties();
      PropertiesUtil.loadIfExist(rawProps, propFile);
      PropertiesUtil.loadIfExist(rawProps, specificPropFile);
      Properties includes = PropertiesUtil.filter(rawProps, PropertiesFilter.NamePrefixPropertiesFilter.createInstance(PROP_INCLUDES));

      includes = PropertiesUtil.replaceVars(includes, PropertiesStrLookup.systemPropertiesLookup());
      final Properties includedProps = new Properties();
      Collection<String> includePaths = PropertiesUtil.values(includes);
      for (String includePath : includePaths) {
        StringTokenizer stok = new StringTokenizer(includePath, ";:");
        while (stok.hasMoreTokens()) {
          String includePropFileName = stok.nextToken();
          PropertiesUtil.loadIfExist(includedProps, new File(includePropFileName));
          PropertiesUtil.replaceVars(includedProps, new StrLookup() {
            @Override
            public String lookup(String name) {
              String value = includedProps.getProperty(name);
              if (value == null)
                value = System.getProperty(name);
              return value;
            }
          });
        }
      }

      // -----------------------------------------------------------------------
      // Now we're loading the server properties with the System properties and
      // included properties as parents (in that order). We're performing
      // variable substitution.

      List<File> configFiles = new ArrayList<File>();
      configFiles.add(propFile);
      if (specificPropFile.exists()) {
        configFiles.add(specificPropFile);
      }
      if (userPropFile.exists()) {
        configFiles.add(userPropFile);
      }
      if (userSpecificPropFile.exists()) {
        configFiles.add(userSpecificPropFile);
      }

      Properties corusProps = new Properties(System.getProperties());
      PropertiesUtil.copy(includedProps, corusProps);
      CorusPropertiesLoader.load(corusProps, configFiles);
      
      // -----------------------------------------------------------------------
      // Overridding config properties with user-data properties

      Properties userDataProperties = userData.getServerProperties();
      // Rendering server properties passed in user-data with self
      userDataProperties = PropertiesUtil.replaceVars(userDataProperties, PropertiesStrLookup.getInstance(userDataProperties));
      
      // Rendering Corus props, given server properties passed in user data
      corusProps = PropertiesUtil.replaceVars(corusProps, PropertiesStrLookup.getInstance(userDataProperties));
      // overwriting with user-data properties
      PropertiesUtil.copy(userDataProperties, corusProps);
      
      // ----------------------------------------------------------------------
      // Determining port: if a port other than the default was passed at the
      // command-line, we're using it. Otherwise, we're using the configured
      // port.
      //
      // We're checking for the port property again since it can be configured
      // in property files loaded after the port was taken from the user-data
      // properties.

      if (port == DEFAULT_PORT && corusProps.getProperty(CorusConsts.PROPERTY_CORUS_PORT) != null) {
        port = Integer.parseInt(corusProps.getProperty(CorusConsts.PROPERTY_CORUS_PORT));
      }
      
      // ----------------------------------------------------------------------
      // Loading Corus read-only file for current instance
      
      CorusReadonlyProperties.loadInto(corusProps, CorusConsts.CORUS_USER_HOME, port);
      
      // ----------------------------------------------------------------------
      // Determining domain: can be specified at command line, or in server
      // properties.

      String domain = null;
      if (corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN) != null) {
        domain = corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN);
      } else if (cmd.containsOption(DOMAIN_OPT, true)) {
        domain = cmd.assertOption(DOMAIN_OPT, true).getValue();
      } else {
        throw new CorusException("Domain must be set; pass -d option to command-line, " 
            + " or configure " + CorusConsts.PROPERTY_CORUS_DOMAIN
            + " as part of " + " corus.properties", ExceptionCode.INTERNAL_ERROR.getFullCode());
      }
      
      if (userData.getDomain().isSet()) {
        domain = userData.getDomain().get();
      }

      System.setProperty(CorusConsts.PROPERTY_CORUS_DOMAIN, domain);

      // ----------------------------------------------------------------------
      // Setting up logging.

      Hierarchy       allLogs        = Hierarchy.getDefaultHierarchy();
      CompositeTarget allLogsTarget  = new CompositeTarget();

      Logger          auditLog       = Hierarchy.getDefaultHierarchy().getLoggerFor(Auditor.ROLE);
      CompositeTarget auditLogTarget = new CompositeTarget();
      auditLog.setPriority(Priority.DEBUG);
      
      Priority p = Priority.DEBUG;

      if (cmd.containsOption(LOG_VERBOSITY_OPT, true)) {
        p = Priority.getPriorityForName(cmd.assertOption(LOG_VERBOSITY_OPT, true).getValue().toUpperCase());

        if (p == null) {
          p = Priority.DEBUG;
        }
      }

      allLogs.setDefaultPriority(p);

      if (cmd.containsOption(LOG_FILE_OPT, false)) {

        File logsDir;

        if (cmd.containsOption(LOG_FILE_OPT, true)) {
          logsDir = new File(cmd.assertOption(LOG_FILE_OPT, true).getValue());
        } else {
          logsDir = new File(corusHome + File.separator + "logs");
        }
        logsDir.mkdirs();

        if (!logsDir.exists()) {
          throw new IOException("Log directory does not exist and could not be created: " + logsDir.getAbsolutePath());
        }

        Formatter allLogsFormatter = FormatterFactory.createDefaultFormatter();
        File      allLogsFile = new File(logsDir.getAbsolutePath() + File.separator + domain + "_" + port + ".log");
        allLogsTarget.addTarget(createFileLogTarget(allLogsFile, allLogsFormatter));
        
        Formatter auditLogFormatter = new AuditLogFormatter();
        File      auditLogFile = new File(logsDir.getAbsolutePath() + File.separator + domain + "_audit_" + port + ".log");
        auditLogTarget.addTarget(createFileLogTarget(auditLogFile, auditLogFormatter));
      } else {
        Formatter allLogsFormatter  = FormatterFactory.createDefaultFormatter();
        StdoutTarget allLogsStdoutTarget = new StdoutTarget(allLogsFormatter);
        allLogsTarget.addTarget(allLogsStdoutTarget);
        
        Formatter auditLogFormatter = new AuditLogFormatter();
        StdoutTarget auditLogStdoutTarget = new StdoutTarget(auditLogFormatter);
        auditLogTarget.addTarget(auditLogStdoutTarget);
      }

      String syslogHost = corusProps.getProperty(PROP_SYSLOG_HOST);
      String syslogPort = corusProps.getProperty(PROP_SYSLOG_PORT);
      String syslogProto = corusProps.getProperty(PROP_SYSLOG_PROTOCOL);

      if (syslogHost != null && syslogPort != null && syslogProto != null) {
        SyslogTarget target = new SyslogTarget(syslogProto, syslogHost, Integer.parseInt(syslogPort));
        allLogsTarget.addTarget(target);
      }

      allLogs.setDefaultLogTarget(allLogsTarget);
      auditLog.setLogTargets(new LogTarget[] {auditLogTarget});

      Logger serverLog = allLogs.getLoggerFor(CorusServer.class.getName());
      
      if (userDataFetchError != null) {
        serverLog.error("Error occurred while attempting to fetch user data. Proceeding with default config. Error was: ", userDataFetchError);
        serverLog.error("Aborting server startup");
        System.exit(1);
      }

      if (propFile.exists()) {
        serverLog.info("Initialized server with properties: " + propFile.getAbsolutePath());
      }
      if (specificPropFile.exists()) {
        serverLog.info("Server properties overridden with specific properties: " + specificPropFile.getAbsolutePath());
      }
      if (userPropFile.exists()) {
        serverLog.info("User-specific properties found, will supercede properties loaded thus far: " + userPropFile.getAbsolutePath());
      }
      if (userSpecificPropFile.exists()) {
        serverLog.info("User-specific override properties file found, will supercede all properties: " + userSpecificPropFile.getAbsolutePath());
      }

      if (serverLog.isDebugEnabled()) {
        serverLog.debug("------------------------ Starting server with following properties: ------------------------");
        Map<String, String> sortedServerProps = PropertiesUtil.map(corusProps);
        for (Map.Entry<String, String> prop : sortedServerProps.entrySet()) {
          serverLog.debug(String.format("%s=%s", prop.getKey(), prop.getValue()));
        }
        serverLog.debug("--------------------------------------------------------------------------------------------");

        if (!userData.getServerTags().isEmpty()) {
          serverLog.debug("Server tags (user data): " + userData.getServerTags());
        }
        if (!userData.getProcessProperties().isEmpty()) {
          serverLog.debug("Process properties (user data): ");
          for (String n : userData.getProcessProperties().stringPropertyNames()) {
            serverLog.debug(String.format("%s=%s", n, userData.getProcessProperties().getProperty(n)));
          }
        }
      }

      // ----------------------------------------------------------------------
      // Exporting server

      if (cmd.containsOption(BIND_ADDR_OPT, true)) {
        String pattern = cmd.assertOption(BIND_ADDR_OPT, true).getValue();
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      } else if (corusProps.getProperty(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN) != null) {
        String pattern = corusProps.getProperty(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN);
        System.setProperty(org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY, pattern);
      }

      CorusTransport transport = new HttpCorusTransport(port);

      // Create Lock file
      File lockFile = new File(corusHome + File.separator + "bin" + File.separator + LOCK_FILE_NAME + "_" + domain + "_" + port);
      IOUtil.createLockFile(lockFile);

      // Initialize Corus, export it and start it
      EventChannel channel = new EventChannel(domain, Conf.newInstance().addProperties(corusProps).addSystemProperties());
      channel.start();

      TCPAddress corusAddr = (TCPAddress) transport.getServerAddress();
      corusProps.setProperty("corus.server.host", corusAddr.getHost());
      corusProps.setProperty("corus.server.port", "" + corusAddr.getPort());
      corusProps.setProperty("corus.server.domain", domain);
      corusProps.setProperty("corus.home", corusHome);
      
      // getting repo role from user data
      if (userData.getRepoRole().isSet()) {
        corusProps.setProperty(CorusConsts.PROPERTY_REPO_TYPE, userData.getRepoRole().get().name());
      }
      
      KeyPair keyPair = KeyPairGenerator.getInstance(corusProps.getProperty(CorusConsts.PROPERTY_KEYPAIR_ALGO, Encryption.DEFAULT_KEY_ALGO)).generateKeyPair();
      
      CorusImpl corus = new CorusImpl(
          corusProps, 
          domain, 
          transport.getServerAddress(), 
          channel, transport, corusHome, keyPair);

      ServerContext context = corus.getServerContext();

      // Adding user data
      if (!userData.getServerTags().isEmpty()) {
        context.getServices().getConfigurator().addTags(userData.getServerTags(), false);
      }
      if (!userData.getProcessProperties().isEmpty()) {
        context.getServices().getConfigurator().addProperties(PropertyScope.PROCESS, userData.getProcessProperties(), new HashSet<String>(), false);
      }

      // keeping reference to stub
      @SuppressWarnings("unused")
      Corus stub = (Corus) transport.exportObject(corus);
      corus.start();

      ServerAddress addr = transport.getServerAddress();

      System.out.println("Corus server (" + CorusVersion.create() + ") started on: " + addr + ", domain: " + domain);

      EventDispatcher dispatcher = context.lookup(EventDispatcher.class);
      
      serverLog.debug("Dispatching user data to internal components");
      dispatcher.dispatch(new CorusUserDataEvent(userData));
      
      dispatcher.dispatch(new ServerStartedEvent(addr));

      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      while (true) {
        Thread.sleep(Integer.MAX_VALUE);
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
  
  private static LogTarget createFileLogTarget(File file, Formatter logFormatter) throws IOException {
    RotateStrategyByTime  rotateStrategy = new RotateStrategyByTime(1000 * 60 * 60 * 24);
    RevolvingFileStrategy fileStrategy = new RevolvingFileStrategy(file, 5);
    RotatingFileTarget    rotatingTarget = new RotatingFileTarget(logFormatter, rotateStrategy, fileStrategy);
    return rotatingTarget;
  }
  
  static final void help() {
    System.out.println();
    System.out.println("Corus server command-line syntax:");
    System.out.println();
    System.out.println("corus [-help] [-c filename] [-d domain] [-p port] [-v DEBUG|INFO|WARN|ERROR] [-f [path_to_log_dir] [-u [user_data_url]]]");
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
    System.out.println("  -u      specifies the user data URL to load Corus user data from.");
    System.out.println();
    System.out.println("  -help   displays this help and exits immediately.");
    System.out.println();
  }

}
