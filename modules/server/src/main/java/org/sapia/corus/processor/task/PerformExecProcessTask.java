package org.sapia.corus.processor.task;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.Interpolation;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.configurator.PropertyMasker;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.deployer.config.EnvImpl;
import org.sapia.corus.numa.NumaModule;
import org.sapia.corus.numa.NumaProcessOptions;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.util.Localhost;

/**
 * Actually performs the execution of the OS process corresponding to the given
 * {@link ProcessInfo}.
 *
 * @author yduchesne
 */
public class PerformExecProcessTask extends Task<Boolean, TaskParams<ProcessInfo, Properties, Void, Void>> {

  @Override
  public Boolean execute(TaskExecutionContext ctx, TaskParams<ProcessInfo, Properties, Void, Void> params) throws Throwable {

    ProcessInfo   info              = params.getParam1();
    Properties    processProperties = params.getParam2();
    ProcessConfig conf              = info.getConfig();
    Process       process           = info.getProcess();
    Distribution  dist              = info.getDistribution();
    PortManager   ports             = ctx.getServerContext().getServices().getPortManager();
    ProcessHookManager processHook  = ctx.getServerContext().getServices().lookup(ProcessHookManager.class);
    NumaModule    numaModule        = ctx.getServerContext().getServices().getNumaModule();

    ProcessorConfiguration processorConf = ctx.getServerContext().getServices().getProcessor().getConfiguration();

    if (conf.getMaxKillRetry() > 0) {
      process.setMaxKillRetry(conf.getMaxKillRetry());
    }

    if (conf.getShutdownTimeout() > 0) {
      process.setShutdownTimeout(conf.getShutdownTimeout());
    }

    if (conf.getPollTimeout() > 0) {
      process.setPollTimeout(conf.getPollTimeout());
    }

    File processDir = makeProcessDir(ctx, info);

    if (processDir == null) {
      return false;
    }

    process.setProcessDir(processDir.getAbsolutePath());
    process.setDeleteOnKill(conf.isDeleteOnKill());

    EnvImpl env = null;

    try {
      env = new EnvImpl(
          ctx.getServerContext().getCorus(),
          ctx.getServerContext().getHomeDir(),
          process.getDistributionInfo().getProfile(),
          dist.getBaseDir(), dist.getCommonDir(),
          process.getProcessDir(),
          getProcessProps(conf, process, dist, ctx, processProperties)
      );
    } catch (PortUnavailableException e) {
      process.releasePorts(ports);
      ctx.error(e);
      return false;
    }

    // Assign numa options
    try {
      if (numaModule.isEnabled()) {
        int numaNodeId = numaModule.getNextNumaNode();
        process.setNumaNode(numaNodeId);
        ctx.info(String.format("Binding process to numa node %s with policy setting: cpuBind=%s memoryBind=%s",
            String.valueOf(numaNodeId), String.valueOf(numaModule.isBindingCpu()), String.valueOf(numaModule.isBindingMemory())));

        NumaProcessOptions.appendProcessOptions(
            numaNodeId,
            numaModule.isBindingCpu(),
            numaModule.isBindingMemory(),
            process.getNativeProcessOptions());
      }
    } catch (Exception e) {
      process.releasePorts(ports);
      ctx.error(e);
      return false;
    }

    OptionalValue<StarterResult> startResult;
    try {
      startResult = conf.toCmdLine(env);
    } catch (Exception e) {
      process.releasePorts(ports);
      ctx.error("Error assinging numa binding", e);
      return false;
    }

    if (startResult.isNull()) {
      ctx.warn(String.format("No executable found for profile: %s", env.getProfile()));
      process.releasePorts(ports);
      return false;
    }
    process.setStarterType(startResult.get().getStarterType());

    // ------------------------------------------------------------------------
    // At this point, only non-hidden properties are passed to the process using -D options.
    // The remaining properties are passed though a .corus-process.hidden.properties file, written
    // to the process directory.
    File           hiddenPropertiesFile = new File(processDir, ".corus-process.hidden.properties");
    Properties     hiddenProperties     = new Properties();
    PropertyMasker masker               = ctx.getServerContext().getServices().getConfigurator().getPropertyMasker();
    for (String n : processProperties.stringPropertyNames()) {
      String v = processProperties.getProperty(n);
      if (masker.isHidden(n)) {
        hiddenProperties.setProperty(n, v);
      }
    }
    OutputStream hiddenPropertiesOutput = ctx.getServerContext().getServices().getFileSystem().getFileOutputStream(hiddenPropertiesFile);
    try {
      hiddenProperties.store(hiddenPropertiesOutput, "Written by Corus");
    } finally {
      try {
        hiddenPropertiesOutput.close();
      } catch (IOException e) {
        // noop
      }
    }

    ctx.info(String.format("Running pre-exec script"));
    conf.preExec(env);

    ctx.info(String.format("Executing process under: %s ---> %s", processDir, startResult.get().getCommand().toString()));

    try {
      ProcessContext processContext = new ProcessContext(process);
      processHook.start(processContext, startResult.get(), callback(ctx));
    } catch (IOException e) {
      ctx.error("Process could not be started", e);
      process.releasePorts(ports);
      return false;
    }

    process.setInteropEnabled(startResult.get().isInteropEnabled());
    process.setStarterType(startResult.get().getStarterType());

    ctx.info(String.format("Process started; corus pid: %s", process.getProcessID()));

    if (process.getOsPid() == null) {
      ctx.warn(String.format("No os pid available for:  %s", process));
    } else {
      ctx.info(String.format("OS pid: %s", process.getOsPid()));
    }

    ctx.getTaskManager().executeBackground(
        new PublishProcessTask(processorConf.getProcessPublishingDiagnosticMaxAttempts()),
        process,
        BackgroundTaskConfig.create()
          .setExecDelay(0).setExecInterval(
              processorConf.getProcessPublishingDiagnosticIntervalMillis()
        )
    );
    return true;
  }

  private File makeProcessDir(TaskExecutionContext ctx, ProcessInfo info) {
    FileSystemModule fs = ctx.getServerContext().lookup(FileSystemModule.class);
    File processDir = new File(FileUtil.toPath(info.getDistribution().getProcessesDir(), info.getProcess().getProcessID()));

    if (info.isRestart() && !fs.exists(processDir)) {
      ctx.warn("Process directory: " + processDir + " does not exist; restart aborted");
      return null;
    } else {
      try {
        fs.createDirectory(processDir);

        if (!fs.exists(processDir)) {
          ctx.warn(String.format("Could not make process directory: %s; startup aborted", processDir.getAbsolutePath()));
          return null;
        }
      } catch (IOException e) {
        ctx.error(String.format("Could not make process directory: %s; startup aborted", processDir.getAbsolutePath()), e);
        return null;
      }
    }
    return processDir;
  }

  Property[] getProcessProps(ProcessConfig conf, Process proc, Distribution dist, TaskExecutionContext ctx, Properties processProperties)
      throws PortUnavailableException {

    PortManager portmgr = ctx.getServerContext().getServices().lookup(PortManager.class);

    List<Property> props = new ArrayList<Property>();
    String host = null;
    String hostName = null;
    try {
      InetAddress inetAddr = Localhost.getPreferredLocalAddress();
      host                 = inetAddr.getHostAddress();
      hostName             = inetAddr.getHostName();
    } catch (Exception e) {
      host = ctx.getServerContext().getCorusHost().getEndpoint().getServerTcpAddress().getHost();
      hostName = ctx.getServerContext().getCorusHost().getHostName();
    }
    int port = ctx.getServerContext().getCorusHost().getEndpoint().getServerTcpAddress().getPort();
    props.add(new Property("corus.home", ctx.getServerContext().getHomeDir()));
    props.add(new Property("corus.server.host", host));
    props.add(new Property("corus.server.host.name", hostName));
    props.add(new Property("corus.server.port", "" + port));
    if (System.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN) != null) {
      props.add(new Property("corus.server.domain", System.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN)));
      props.add(new Property(RemoteInitialContextFactory.UBIK_DOMAIN_NAME, System.getProperty(CorusConsts.PROPERTY_CORUS_DOMAIN)));
    }
    props.add(new Property("corus.distribution.name", dist.getName()));
    props.add(new Property("corus.distribution.version", dist.getVersion()));
    props.add(new Property("corus.process.name", conf.getName()));
    props.add(new Property("corus.process.dir", proc.getProcessDir()));
    props.add(new Property("corus.process.id", proc.getProcessID()));
    if (proc.getOsPid() != null) {
      props.add(new Property("corus.process.os.pid", proc.getOsPid()));
    }
    props.add(new Property("corus.process.poll.interval", "" + conf.getPollInterval()));
    props.add(new Property("corus.process.status.interval", "" + conf.getStatusInterval()));
    props.add(new Property("corus.process.profile", proc.getDistributionInfo().getProfile()));
    props.add(new Property("user.dir", dist.getCommonDir()));


    // ------------------------------------------------------------------------
    // Performing variable interpolation for process properties passed in
    // from Corus

    Map<String, String> coreProps = new HashMap<>();
    for (Property p : props) {
      coreProps.put(p.getName(), p.getValue());
    }

    // adding environment variables as fallback
    CompositeStrLookup vars = new CompositeStrLookup()
      .add(StrLookup.mapLookup(coreProps))
      .add(StrLookup.mapLookup(System.getenv()));

    processProperties = Interpolation.interpolate(
        processProperties,
        vars,
        conf.getInterpolationPasses() <= 0 ? ProcessConfig.DEFAULT_INTERPOLATION_PASSES : conf.getInterpolationPasses()
    );

    // ------------------------------------------------------------------------
    // Processing double quotes to values, and then adding to Property list

    for (String name : processProperties.stringPropertyNames()) {
      String value = processProperties.getProperty(name);
      if (value != null) {
        if (StringUtils.isNotEmpty(value)) {
          boolean toEncloseInDoubleQuotes = (value.indexOf(' ') >= 0);
          if (value.charAt(0) == '\"' && value.charAt(value.length()-1) == '\"') {
              // Temporarely removing surrounding double quotes
              value = value.substring(1, value.length()-1);
              toEncloseInDoubleQuotes = true;
          }

          // Escaping any double quotes
          value = value.replace("\"", "\\\"");

          // Surrounding with double quotes
          if (toEncloseInDoubleQuotes) {
              value = "\"" + value + "\"";
          }
        }
        PropertyMasker masker = ctx.getServerContext().getServices().getConfigurator().getPropertyMasker();
        if (!masker.isHidden(name)) {
          ctx.info("Passing process property: " + name + "=" + value);
          props.add(new Property(name, value));
        }
      }
    }

    // ------------------------------------------------------------------------
    // Adding port values

    List<Port> ports = conf.getPorts();
    Set<String> added = new HashSet<String>();
    for (int i = 0; i < ports.size(); i++) {
      Port p = ports.get(i);
      if (!added.contains(p.getName())) {
        int portInt = portmgr.aquirePort(p.getName());
        props.add(new Property("corus.process.port." + p.getName(), Integer.toString(portInt)));
        proc.addActivePort(new ActivePort(p.getName(), portInt));
        added.add(p.getName());
      }
    }

    return props.toArray(new Property[props.size()]);
  }

  private LogCallback callback(final TaskExecutionContext ctx) {
    return new LogCallback() {
      @Override
      public void error(String msg) {
        ctx.error(msg);
      }
      @Override
      public void info(String msg) {
        ctx.info(msg);
      }
      @Override
      public void debug(String msg) {
        ctx.debug(msg);
      }
    };
  }

}
