package org.sapia.corus.processor.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.configurator.InternalConfigurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.core.Consts;
import org.sapia.corus.deployer.config.EnvImpl;
import org.sapia.corus.processor.NativeProcess;
import org.sapia.corus.processor.NativeProcessFactory;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.tasks.TaskFactory;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.util.Localhost;

/**
 * Implements the default {@link ProcessorTaskStrategy}.
 * 
 * @author yduchesne
 * 
 */
public class ProcessorTaskStrategyImpl implements ProcessorTaskStrategy {

  public boolean attemptKill(TaskExecutionContext ctx,
      ProcessTerminationRequestor requestor, Process proc, int currentRetryCount) {
    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
      ctx.info("Process " + proc.getProcessID() + " has confirmed shutdown");
      return true;
    }
    ctx.info("Killing process " + proc + ". Attempt: " + currentRetryCount
        + "; requestor: " + requestor);
    proc.kill(requestor);
    proc.save();
    return false;
  }
  public void cleanupProcess(TaskExecutionContext ctx, Process proc){
    if (proc.getProcessDir() != null) {
      if (proc.isDeleteOnKill()) {
        File f = new File(proc.getProcessDir());
        try{
          TaskFactory.newDeleteDirTask(f).execute(ctx);
          if (f.exists()) {
            ctx.warn("Could not destroy process directory: "
                + f.getAbsolutePath());
          }
        }catch(Throwable e){
          if (f.exists()) {
            ctx.warn("Could not destroy process directory: "
                + f.getAbsolutePath(), e);
          }
        }
      }
    }

    ctx.getServerContext().getServices().getProcesses().getActiveProcesses()
        .removeProcess(proc.getProcessID());
    if (proc.isDeleteOnKill()) {
      ctx.warn("Process successfully terminated and cleaned up: "
          + proc.getProcessID());
    } else {
      ctx.warn("Process successfully terminated: " + proc.getProcessID());
    }
  }

  public boolean execCmdLine(TaskExecutionContext ctx, File processDir,
      CmdLine cmdLine, Process process) {
    NativeProcess nativeProc = NativeProcessFactory.newNativeProcess();

    try {
      process.setOsPid(nativeProc.exec(ctx, processDir, cmdLine));
    } catch (IOException e) {
      ctx.error("Process could not be started", e);
      return false;
    }

    ctx.info("Process started; corus pid: " + process.getProcessID());

    if (process.getOsPid() == null) {
      ctx.warn("No os pid available for:  " + process);
    } else {
      ctx.info("OS pid: " + process.getOsPid());
    }
    return true;
  }

  public boolean execProcess(TaskExecutionContext ctx, ProcessInfo info,
      Properties processProperties){

    ProcessConfig conf = info.getConfig();
    Process process = info.getProcess();
    Distribution dist = info.getDistribution();
    PortManager ports = ctx.getServerContext().getServices().lookup(
        PortManager.class);

    if (conf.getMaxKillRetry() >= 0) {
      process.setMaxKillRetry(conf.getMaxKillRetry());
    }

    if (conf.getShutdownTimeout() >= 0) {
      process.setShutdownTimeout(conf.getShutdownTimeout());
    }

    File processDir = makeProcessDir(ctx, info);

    if (processDir == null) {
      return false;
    }
    
    process.setProcessDir(processDir.getAbsolutePath());
    process.setDeleteOnKill(conf.isDeleteOnKill());

    EnvImpl env = null;

    try {
      env = new EnvImpl(ctx.getServerContext().getHomeDir(), process.getDistributionInfo().getProfile(), dist
          .getBaseDir(), dist.getCommonDir(), process.getProcessDir(),
          getProcessProps(conf, process, dist, ctx, processProperties));
    } catch (PortUnavailableException e) {
      process.releasePorts(ports);
      ctx.error(e);
      return false;
    }

    CmdLine cmd;
    try {
      cmd = conf.toCmdLine(env);
    } catch (Exception e) {
      process.releasePorts(ports);
      ctx.error(e);
      return false;
    }

    if (cmd == null) {
      ctx.warn("No executable found for profile: " + env.getProfile());
      process.releasePorts(ports);
      return false;
    }

    ctx.info("Executing process under: " + processDir + " ---> "
        + cmd.toString());
    
    boolean executed = execCmdLine(ctx, processDir, cmd, process);
    if (!executed) {
      process.releasePorts(ports);
    }
    return executed;
  }

  @SuppressWarnings(value="unchecked")
  private Property[] getProcessProps(ProcessConfig conf, Process proc,
      Distribution dist, TaskExecutionContext ctx, Properties processProperties)
      throws PortUnavailableException {

    InternalConfigurator configurator = ctx.getServerContext().getServices().lookup(
        InternalConfigurator.class);
    PortManager portmgr = ctx.getServerContext().getServices().lookup(
        PortManager.class);

    List<Property> props = new ArrayList<Property>(10);
    String host = null;
    try {
      host = Localhost.getLocalAddress().getHostAddress();
    } catch (Exception e) {
      host = ctx.getServerContext().getServerAddress().getHost();
    }
    int port = ctx.getServerContext().getServerAddress().getPort();
    props.add(new Property("corus.home", ctx.getServerContext().getHomeDir()));
    props.add(new Property("corus.server.host", host));
    props.add(new Property("corus.server.port", "" + port));
    if (System.getProperty(Consts.PROPERTY_CORUS_DOMAIN) != null) {
      props.add(new Property("corus.server.domain", System
          .getProperty(Consts.PROPERTY_CORUS_DOMAIN)));
      props.add(new Property(RemoteInitialContextFactory.UBIK_DOMAIN_NAME,
          System.getProperty(Consts.PROPERTY_CORUS_DOMAIN)));
    }
    props.add(new Property("corus.distribution.name", dist.getName()));
    props.add(new Property("corus.distribution.version", dist.getVersion()));
    props.add(new Property("corus.process.dir", proc.getProcessDir()));
    props.add(new Property("corus.process.id", proc.getProcessID()));
    props.add(new Property("corus.process.poll.interval", ""
        + conf.getPollInterval()));
    props.add(new Property("corus.process.status.interval", ""
        + conf.getStatusInterval()));
    props.add(new Property("corus.process.profile", proc.getDistributionInfo()
        .getProfile()));
    props.add(new Property("user.dir", dist.getCommonDir()));

    Properties allProps = new Properties(processProperties);
    Properties confProps = configurator.getInternalProperties(PropertyScope.PROCESS);

    Enumeration names = confProps.propertyNames();

    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      allProps.put(name, confProps.getProperty(name));
    }

    names = processProperties.propertyNames();

    // process properties...
    while (names.hasMoreElements()) {
      String name = (String) names.nextElement();
      String value = processProperties.getProperty(name);
      if (value != null) {
        if (value.indexOf(' ') > 0) {
          value = "\"" + value + "\"";
        }
        ctx.info("Passing process property: " + name + "=" + hideIfPassword(name, value));
        props.add(new Property(name, value));
      }
    }

    // process ports...
    List<Port> ports = conf.getPorts();
    Set<String> added = new HashSet<String>();
    for (int i = 0; i < ports.size(); i++) {
      Port p = ports.get(i);
      if (!added.contains(p.getName())) {
        int portInt = portmgr.aquirePort(p.getName());
        props.add(new Property("corus.process.port." + p.getName(), Integer
            .toString(portInt)));
        proc.addActivePort(new ActivePort(p.getName(), portInt));
        added.add(p.getName());
      }
    }

    return (Property[]) props.toArray(new Property[props.size()]);
  }

  public boolean forcefulKill(TaskExecutionContext ctx,
      ProcessTerminationRequestor requestor, String corusPid){
    boolean killSuccessful = false;

    try {

      PortManager ports = ctx.getServerContext().getServices().lookup(
          PortManager.class);
      Processor processor = ctx.getServerContext().getServices().lookup(
          Processor.class);
      ProcessRepository processes = ctx.getServerContext().getServices()
          .getProcesses();
      Process process = processes.getActiveProcesses().getProcess(corusPid);

      
      ctx.warn("Process " + process.getProcessID() + " did not confirm kill: "
          + process + "; requestor: " + requestor);

      // try forcefull kill if OS pid not null...
      if (process.getOsPid() != null) {
        try {
          doNativeKill(ctx, process);
          killSuccessful = true;
        } catch (IOException e) {
          ctx.warn("Error performing OS kill on process " + process.getOsPid());
          ctx.error(e);
        }
      } else {
        ctx.warn("Process " + corusPid + " is stalled but could not be killed");
      }

      process.releasePorts(ports);
      
      // if shutdown was initiated by Corus server, restart process
      // automatically (if restarted interval threshold is respected)
      if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER
          && processor.getConfiguration().getRestartIntervalMillis() > 0) {
        ctx.debug("Preparing for restart");
        ctx.debug("Process creation time: "
            + new Date(process.getCreationTime()));
        ctx.debug("Current time: " + new Date());
        ctx.debug("Restart interval: "
            + processor.getConfiguration().getRestartIntervalMillis() + " millis");
        // if no OS pid, then process could not be forcefully killed...
        if (process.getOsPid() == null) {
          ctx.warn("Not restarting process: " + process.getProcessID()
              + "; did not confirm shutdown");
          ctx
              .warn("Could not be forcefully killed (because it does not have an OS pid)");
          ctx
              .warn("Might be stalled... Make sure that you do not have a process in limbo");
          cleanupProcess(ctx, process);
          onNoOsPid();
        } else if (((System.currentTimeMillis() - process.getCreationTime()) < processor
            .getConfiguration().getRestartIntervalMillis())) {
          ctx.warn("Process will not be restarted; not enough time since last restart");
          onRestartThresholdInvalid();
          cleanupProcess(ctx, process);
        } else {
          ctx.warn("Restarting Process: " + process);
          processes.getActiveProcesses().removeProcess(process.getProcessID());
          processes.getProcessesToRestart().addProcess(process);
          restartProcess(ctx, process);
          onRestarted();
        }
      } else {
        ctx.warn("Process " + process.getProcessID() + " terminated");
      }
    } catch (ProcessNotFoundException e) {
      ctx.error(e);
    }

    return killSuccessful;
  }

  public void killConfirmed(TaskExecutionContext ctx, Process process){
    PortManager ports = ctx.getServerContext().getServices().lookup(
        PortManager.class);
    ctx.info("Process kill confirmed: " + process.getProcessID());
    process.releasePorts(ports);
    cleanupProcess(ctx, process);
    ctx.warn("Process " + process.getProcessID() + " terminated");
  }

  public void killProcess(TaskExecutionContext ctx,
      ProcessTerminationRequestor requestor, Process proc) {
    ProcessorConfiguration processorConf = ctx.getServerContext().getServices()
        .lookup(Processor.class).getConfiguration();
    KillTask kill = new KillTask(requestor, proc.getProcessID(), proc.getMaxKillRetry());
    ctx.getTaskManager().executeBackground(
        kill, 
        BackgroundTaskConfig.create()
          .setExecDelay(0)
          .setExecInterval(processorConf.getKillIntervalMillis()));
  }

  public File makeProcessDir(TaskExecutionContext ctx, ProcessInfo info) {
    File processDir = new File(info.getDistribution().getProcessesDir()
        + File.separator + info.getProcess().getProcessID());

    if (info.isRestart() && !processDir.exists()) {
      ctx.warn(
          "Process directory: " + processDir
              + " does not exist; restart aborted");
      return null;
    } else {
      processDir.mkdirs();

      if (!processDir.exists()) {
        ctx.warn(
            "Could not make process directory: " + processDir
                + "; startup aborted");

        return null;
      }
    }
    return processDir;
  }

  public boolean restartProcess(TaskExecutionContext ctx, Process process) {
    
    Deployer          deployer  = ctx.getServerContext().getServices().lookup(Deployer.class);
    ProcessRepository repo      = ctx.getServerContext().getServices().getProcesses();
    Distribution      dist; 

    ctx.debug("Executing process");
    try{
      Arg nameArg = ArgFactory.exact(process.getDistributionInfo().getName());
      Arg versionArg = ArgFactory.exact(process.getDistributionInfo().getVersion());      
      
      dist = deployer.getDistribution(nameArg, versionArg);
    }catch(DistributionNotFoundException e){
      ctx.error("Could not find corresponding distribution; process " + process.getProcessID() + " will not be restarted", e);
      return false;
    }    
    
    if(repo.getProcessesToRestart().containsProcess(process.getProcessID())){
      ProcessConfig processConf = dist.getProcess(process.getDistributionInfo().getProcessName());
      try{
        if(execProcess(ctx, new ProcessInfo(process, dist, processConf , true), ctx.getServerContext().getProcessProperties())){
          repo.getProcessesToRestart().removeProcess(process.getProcessID());
          process.touch();
          process.clearCommands();
          process.setStatus(Process.LifeCycleStatus.ACTIVE);        
          repo.getActiveProcesses().addProcess(process);
          return true;
        }
        else{
          return false;
        }
      }catch(Exception e){
        ctx.error("Error trying to restart " + process.getProcessID() + " will not be restarted", e);
        return false;
      }    
    }
    else{
      try{
        Arg nameArg = ArgFactory.exact(process.getDistributionInfo().getName());
        Arg versionArg = ArgFactory.exact(process.getDistributionInfo().getVersion());      
        
        dist = deployer.getDistribution(nameArg, versionArg);
      }catch(DistributionNotFoundException e){
        ctx.error("Could not find corresponding distribution; process " + process.getProcessID() + " will not be restarted", e);
        return false;
      }catch(Exception e){
        ctx.error("Error trying to restart " + process.getProcessID() + " will not be restarted", e);
        return false;
      }    
      
      ExecTask exec = new ExecTask(dist, dist.getProcess(process.getDistributionInfo().getProcessName()), process.getDistributionInfo().getProfile());
      ctx.getTaskManager().executeAndWait(exec);
      return true;
    }
  }
  
  /////////// restricted methods
  
  protected void doNativeKill(TaskExecutionContext ctx, Process proc) throws IOException{
    NativeProcessFactory.newNativeProcess().kill(ctx, proc.getOsPid());    
  }
  
  protected void onNoOsPid(){}
  
  protected void onRestartThresholdInvalid(){}
  
  protected void onRestarted(){}
  
  private String hideIfPassword(String name, String value){
    if(name.contains("password")){
      return "********";
    }
    else return value;
  }
}
