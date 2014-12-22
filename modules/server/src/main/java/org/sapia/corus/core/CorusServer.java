package org.sapia.corus.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.CorusVersion;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.cloud.CorusUserData;
import org.sapia.corus.cloud.CorusUserDataFactory;
import org.sapia.corus.log.CompositeTarget;
import org.sapia.corus.log.FormatterFactory;
import org.sapia.corus.log.StdoutTarget;
import org.sapia.corus.log.SyslogTarget;
import org.sapia.corus.util.CorusTimestampOutputStream;
import org.sapia.corus.util.IOUtil;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Conf;

/**
 * This class is the entry point called from the 'java' command line.
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

  @SuppressWarnings({ "deprecation" })
  public static void main(String[] args) {
    
    System.setOut(new CorusTimestampOutputStream(System.out));
    System.setErr(new CorusTimestampOutputStream(System.err));
    
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

      CmdLine cmd;
      if (args.length == 0) {
        cmd = new CmdLine();
      } else {
        cmd = CmdLine.parse(args);
      }

      if (CliUtils.isHelp(cmd)) {
        help();
        return;
      }

      CorusUserData userData;
      if (cmd.containsOption(USER_DATA, false)) {
        Option userDataOpt = cmd.assertOption(USER_DATA, false);
        if (userDataOpt.getValue() != null) {
          userData = CorusUserDataFactory.fetchUserData(URI.create(userDataOpt.getValue()));
        } else {
          userData = CorusUserDataFactory.fetchUserData();
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
      File userPropFile = FilePath.newInstance()
          .addCorusUserDir()
          .setRelativeFile("corus.properties")
          .createFile();

      File userSpecificPropFile = FilePath.newInstance()
          .addCorusUserDir()
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

      PropertiesUtil.copy(userData.getServerProperties(), corusProps);

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
      // Loading Corus domain file for current instance
      
      File corusDomainPropsFile = FilePath.newInstance()
          .addCorusUserDir()
          .setRelativeFile(".corus_domain_" + port + ".properties")
          .createFile();
      PropertiesUtil.loadIfExist(corusProps, corusDomainPropsFile);
      
      // ----------------------------------------------------------------------
      // Loading Corus repo file for current instance
      
      File repoFile = FilePath.newInstance()
          .addCorusUserDir()
          .setRelativeFile(".corus_repo_" + port + ".properties")
          .createFile();
      PropertiesUtil.loadIfExist(corusProps, repoFile);
      
      // ----------------------------------------------------------------------
      // Determining domain: can be specified at command line, or in server
      // properties.

      String domain = null;
      if (cmd.containsOption(DOMAIN_OPT, true)) {
        domain = cmd.assertOption(DOMAIN_OPT, true).getValue();
      } else if (corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN) != null) {
        domain = corusProps.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN);
      } else {
        throw new CorusException("Domain must be set; pass -d option to command-line, " + " or configure " + CorusConsts.PROPERTY_CORUS_DOMAIN
            + " as part of " + " corus.properties", ExceptionCode.INTERNAL_ERROR.getFullCode());
      }

      System.setProperty(CorusConsts.PROPERTY_CORUS_DOMAIN, domain);

      // ----------------------------------------------------------------------
      // Setting up logging.

      Hierarchy h = Hierarchy.getDefaultHierarchy();
      CompositeTarget logTarget = null;

      Priority p = Priority.DEBUG;

      if (cmd.containsOption(LOG_VERBOSITY_OPT, true)) {
        p = Priority.getPriorityForName(cmd.assertOption(LOG_VERBOSITY_OPT, true).getValue());

        if (p == null) {
          p = Priority.DEBUG;
        }
      }

      h.setDefaultPriority(p);

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

        Formatter formatter = FormatterFactory.createDefaultFormatter();
        RotateStrategyByTime strategy = new RotateStrategyByTime(1000 * 60 * 60 * 24);

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

      String syslogHost = corusProps.getProperty(PROP_SYSLOG_HOST);
      String syslogPort = corusProps.getProperty(PROP_SYSLOG_PORT);
      String syslogProto = corusProps.getProperty(PROP_SYSLOG_PROTOCOL);

      if (syslogHost != null && syslogPort != null && syslogProto != null) {
        SyslogTarget target = new SyslogTarget(syslogProto, syslogHost, Integer.parseInt(syslogPort));
        logTarget.addTarget(target);
      }

      h.setDefaultLogTarget(logTarget);

      Logger serverLog = h.getLoggerFor(CorusServer.class.getName());

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
      EventChannel channel = new EventChannel(domain, new Conf().addSystemProperties());
      channel.start();

      CorusImpl corus = new CorusImpl(corusProps, domain, transport.getServerAddress(), channel, transport, corusHome);

      ServerContext context = corus.getServerContext();

      // Adding user data
      if (!userData.getServerTags().isEmpty()) {
        context.getServices().getConfigurator().addTags(userData.getServerTags());
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
