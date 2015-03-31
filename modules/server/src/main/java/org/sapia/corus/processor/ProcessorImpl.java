package org.sapia.corus.processor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.exceptions.processor.TooManyProcessInstanceException;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.event.UndeploymentEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcStatus;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.database.CachingDbMap;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.interop.Status;
import org.sapia.corus.processor.task.BootstrapExecConfigStartTask;
import org.sapia.corus.processor.task.EndUserExecConfigStartTask;
import org.sapia.corus.processor.task.KillTask;
import org.sapia.corus.processor.task.MultiExecTask;
import org.sapia.corus.processor.task.ProcessAutoshutDownConfirmTask;
import org.sapia.corus.processor.task.ProcessCheckTask;
import org.sapia.corus.processor.task.PublishConfigurationChangeTask;
import org.sapia.corus.processor.task.RestartTask;
import org.sapia.corus.processor.task.ResumeTask;
import org.sapia.corus.processor.task.SuspendTask;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleFactory;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link Processor} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = Processor.class)
@Remote(interfaces = Processor.class)
public class ProcessorImpl extends ModuleHelper implements Processor {

  @Autowired
  private ProcessorConfiguration configuration;
  @Autowired
  DbModule db;
  @Autowired
  private Deployer deployer;
  @Autowired
  private TaskManager taskman;
  @Autowired
  private EventDispatcher events;
  @Autowired
  private HttpModule http;
  @Autowired
  private PortManager portManager;
  @Autowired
  private OsModule os;

  private ProcessRepository processes;
  private ExecConfigDatabase execConfigs;
  
  private boolean isPublishProcessConfigurationChangeEnabled = false;
  
  public ProcessorConfiguration getConfiguration() {
    return configuration;
  }
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  protected void setDb(DbModule db) {
    this.db = db;
  }
  
  protected void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }
  
  protected void setTaskManager(TaskManager taskman) {
    this.taskman = taskman;
  }
  
  protected void setConfiguration(ProcessorConfiguration configuration) {
    this.configuration = configuration;
  }
  
  protected void setEvents(EventDispatcher events) {
    this.events = events;
  }
  
  protected void setHttpModule(HttpModule http) {
    this.http = http;
  }
  
  protected boolean isPublishProcessConfigurationChangeEnabled() {
    return this.isPublishProcessConfigurationChangeEnabled;
  }

  // --------------------------------------------------------------------------
  // Lifecycle
  
  public void init() throws Exception {

    services().getTaskManager().registerThrottle(ProcessorThrottleKeys.PROCESS_EXEC,
        ThrottleFactory.createTimeIntervalThrottle(TimeUnit.MILLISECONDS, configuration.getStartIntervalMillis()));

    // // intializing process repository
    execConfigs = new ExecConfigDatabaseImpl(db.getDbMap(String.class, ExecConfig.class, "processor.execConfigs"));
    services().bind(ExecConfigDatabase.class, execConfigs);

    ProcessDatabase processDb = new ProcessDatabaseImpl(new CachingDbMap<String, Process>(db.getDbMap(String.class, Process.class, "processor.processes")));
    processes = new ProcessRepositoryImpl(processDb);
    services().bind(ProcessRepository.class, processes);

    // here we "touch" the process objects to prevent their automatic
    // termination (the Corus server might have been down for a period
    // of time that is longer then some process' tolerated idle delay).
    List<Process> processes = (List<Process>) processDb.getProcesses(ProcessCriteria.builder().lifecycles(
        LifeCycleStatus.ACTIVE, LifeCycleStatus.RESTARTING, LifeCycleStatus.KILL_CONFIRMED
    ).build());
    Process proc;
    
    for (int i = 0; i < processes.size(); i++) {
      proc = (Process) processes.get(i);
      if (proc.getStatus() == LifeCycleStatus.KILL_CONFIRMED) {
        // making sure we're removing process that might not have been removed
        // before the last Corus shutdown.
        log.debug("Removing stale process object - is confirmed as killed");
        processDb.removeProcess(proc.getProcessID());
      } else if (proc.getStatus() == LifeCycleStatus.RESTARTING || proc.getStatus() == LifeCycleStatus.KILL_REQUESTED) {
        // set process in auto-restart to active. if it is down, 
        // it will not poll and enter the shutdown procedure, 
        // eventually being clean up definitively.
        proc.setStatus(LifeCycleStatus.ACTIVE);
        proc.save();
      } else {
        proc.touch();
        proc.save();
      }
    }
    
    if (!configuration.autoRestartStaleProcesses()) {
      log.warn("Process auto-restart is disabled. Stale processes will not automatically be restarted");
    }
  }

  public void start() throws Exception {
    ProcessorExtension ext = new ProcessorExtension(this, serverContext());
    http.addHttpExtension(ext);

    if (configuration.isBootExecEnabled()) {
      BootstrapExecConfigStartTask boot = new BootstrapExecConfigStartTask();

      taskman.executeBackground(boot, null,
          BackgroundTaskConfig.create().setExecDelay(configuration.getBootExecDelayMillis()).setExecInterval(configuration.getStartIntervalMillis()));
    } else {
      log.warn("Automatic startup of processes at boot time is disabled for this node");
    }

    ProcessCheckTask check = new ProcessCheckTask();
    taskman.executeBackground(check, null,
        BackgroundTaskConfig.create().setExecDelay(0).setExecInterval(configuration.getProcessCheckIntervalMillis()));

    events.addInterceptor(UndeploymentEvent.class, new ProcessorInterceptor());
    
    // Waiting for this service to be running before activating the process update feature (to avoid any startup mixed-up)
    isPublishProcessConfigurationChangeEnabled = Boolean.parseBoolean(
        serverContext().getCorusProperties().getProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false"));
    events.addInterceptor(PropertyChangeEvent.class, new PropertyChangeInterceptor()); 
  }

  public void dispose() {
  }

  /*
   * //////////////////////////////////////////////////////////////////// Module
   * INTERFACE METHODS
   * ////////////////////////////////////////////////////////////////////
   */

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return Processor.ROLE;
  }

  /*
   * ////////////////////////////////////////////////////////////////////
   * Processor INTERFACE METHODS
   * ////////////////////////////////////////////////////////////////////
   */

  @Override
  public ProgressQueue execConfig(ExecConfigCriteria criteria) {
    ProgressQueue progress = new ProgressQueueImpl();
    EndUserExecConfigStartTask start = new EndUserExecConfigStartTask(criteria);
    try {
      taskman.executeAndWait(start, null, TaskConfig.create(new TaskLogProgressQueue(progress))).get();
    } catch (InvocationTargetException e) {
      progress.error(e.getCause());
    } catch (InterruptedException e) {
      progress.error(e);
    }

    return progress;

  }
  
  @Override
  public void setExecConfigEnabled(ExecConfigCriteria criteria, boolean enabled) {
    List<ExecConfig> configs = this.execConfigs.getConfigsFor(criteria);
    for (ExecConfig c : configs) {
      c.setEnabled(enabled);
      c.save();
    }
  }
  
  @Override
  public ProgressQueue exec(ProcessCriteria criteria, int instances) throws TooManyProcessInstanceException {
    try {
      List<Distribution> dists = deployer.getDistributions(criteria.getDistributionCriteria());
      if (dists.size() == 0) {
        throw new DistributionNotFoundException(String.format("No distribution for %s, %s", criteria.getDistribution(), criteria.getVersion()));
      }
      ProgressQueueImpl q = new ProgressQueueImpl();
      ProcessDependencyFilter filter = new ProcessDependencyFilter(q);
      for (int i = 0; i < dists.size(); i++) {
        Distribution dist = dists.get(i);
        List<ProcessConfig> configs;
        if (criteria.getName() == null) {
          configs = new ArrayList<ProcessConfig>();
          List<ProcessConfig> temp = dist.getProcesses();
          for (ProcessConfig pc : temp) {
            if (!pc.isInvoke()) {
              configs.add(pc);
            }
          }
        } else {
          configs = dist.getProcesses(criteria.getName());
        }
        if (configs.size() == 0) {
          q.warning(String.format("Could not find any process to start for: %s", criteria));
        } else {
          for (ProcessConfig config : configs) {
            int activeCount = processes.getActiveProcessCountFor(criteria);
            int totalCount = activeCount + instances;
            if (config.getMaxInstances() > 0 && (totalCount > config.getMaxInstances())) {
              throw new TooManyProcessInstanceException("Too many process instances for : " + config.getName() + "(" + dist.getName() + " "
                  + dist.getVersion() + ")" + ", requested: " + instances + ", currently active: " + activeCount + ", maximum permitted: "
                  + config.getMaxInstances());
            }
            filter.addRootProcess(dist, config, criteria.getProfile().get(), instances);
          }
        }
      }
      List<ProcessRef> toStart = new ArrayList<ProcessRef>();
      filter.filterDependencies(deployer, this);
      for (ProcessRef fp : filter.getFilteredProcesses()) {
        q.info("Process execution requested for: " + fp);
        ProcessRef copy = fp.getCopy();
        toStart.add(copy);
      }

      taskman.execute(new MultiExecTask(), toStart);

      q.close();
      return q;
    } catch (Exception e) {
      ProgressQueue q = new ProgressQueueImpl();
      q.error(e);
      q.close();

      return q;
    }
  }

  @Override
  public void addExecConfig(ExecConfig conf) {
    if (conf.getName() == null) {
      throw new MissingDataException("Execution configuration must have a name");
    }
    this.execConfigs.addConfig(conf);
  }

  @Override
  public List<ExecConfig> getExecConfigs(ExecConfigCriteria criteria) {
    return execConfigs.getConfigsFor(criteria);
  }

  @Override
  public void removeExecConfig(ExecConfigCriteria criteria) {
    execConfigs.removeConfigsFor(criteria);
  }

  @Override
  public void kill(ProcessCriteria criteria, KillPreferences pref) {
    ProcessCriteria killCriteria = ProcessCriteria.builder().copy(criteria).lifecycles(
        LifeCycleStatus.ACTIVE, LifeCycleStatus.STALE, LifeCycleStatus.RESTARTING
    ).build();
    List<Process> procs = processes.getProcesses(killCriteria);
 
    for (Process proc : procs) {
      if (pref.isSuspend()) {
        SuspendTask susp = new SuspendTask(proc.getMaxKillRetry());
        susp.setHardKill(pref.isHard());
        taskman.executeBackground(susp, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN), BackgroundTaskConfig.create()
            .setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
      } else {
        KillTask kill = new KillTask(proc.getMaxKillRetry());
        kill.setHardKill(pref.isHard());
        taskman.executeBackground(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN), BackgroundTaskConfig.create()
            .setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
      }
    }
  }

  @Override
  public void kill(String corusPid, KillPreferences pref) throws ProcessNotFoundException {

    Process proc = processes.getProcess(corusPid);

    if (proc.getStatus() == LifeCycleStatus.ACTIVE || proc.getStatus() == LifeCycleStatus.STALE || proc.getStatus() == LifeCycleStatus.RESTARTING) {
      if (pref.isSuspend()) {
        SuspendTask susp = new SuspendTask(proc.getMaxKillRetry());
        susp.setHardKill(pref.isHard());
        taskman.executeBackground(susp, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN), BackgroundTaskConfig.create()
            .setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
      } else {
        KillTask kill = new KillTask(proc.getMaxKillRetry());
        kill.setHardKill(pref.isHard());
        taskman.executeBackground(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN), BackgroundTaskConfig.create()
            .setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
      }
    } else {
      throw new ProcessNotFoundException("No process found for ID: " + corusPid);
    }
  }

  @Override
  public void confirmShutdown(String corusPid) throws ProcessNotFoundException {
    Process process = getProcess(corusPid);

    // if the process is currently active, it means it shut down autonomously
    if (process.getStatus() == LifeCycleStatus.ACTIVE) {
      taskman.execute(new ProcessAutoshutDownConfirmTask(), process);

      // else, just making sure the status is set to confirm, the current
      // background kill task will complete the work based on that status being set.
    } else if (process.getStatus() != LifeCycleStatus.KILL_CONFIRMED) {
      process.confirmKilled();
      process.save();
    }
  }

  @Override
  public void restartByAdmin(String pid, KillPreferences prefs) throws ProcessNotFoundException {
    doRestart(pid, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN, prefs);
  }

  @Override
  public void restart(String pid, KillPreferences prefs) throws ProcessNotFoundException {
    doRestart(pid, ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS, prefs);
  }

  @Override
  public void restart(ProcessCriteria criteria, KillPreferences prefs) {
    ProcessCriteria restartCriteria = ProcessCriteria.builder().copy(criteria).lifecycles(
        LifeCycleStatus.ACTIVE, LifeCycleStatus.STALE, LifeCycleStatus.RESTARTING
    ).build();
    
    List<Process> procs = processes.getProcesses(restartCriteria);

    for (int i = 0; i < procs.size(); i++) {
      Process proc = (Process) procs.get(i);
      RestartTask restart = new RestartTask(proc.getMaxKillRetry());
      restart.setHardKill(prefs.isHard());
      taskman.executeBackground(restart, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN), BackgroundTaskConfig.create()
          .setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
    }
  }

  private void doRestart(String pid, ProcessTerminationRequestor origin, KillPreferences prefs) throws ProcessNotFoundException {
    Process proc = processes.getProcess(pid);
    
    if (proc.getStatus() == LifeCycleStatus.ACTIVE || proc.getStatus() == LifeCycleStatus.STALE || proc.getStatus() == LifeCycleStatus.RESTARTING) {
      RestartTask restart = new RestartTask(proc.getMaxKillRetry());
      restart.setHardKill(prefs.isHard());
  
      taskman.executeBackground(restart, TaskParams.createFor(proc, origin),
          BackgroundTaskConfig.create().setExecDelay(0).setExecInterval(configuration.getKillIntervalMillis()));
    }
  }

  @Override
  public ProgressQueue resume() {
    return resume(ProcessCriteria.builder().all());
  }

  @Override
  public ProgressQueue resume(ProcessCriteria processCriteria) {
    ProcessCriteria resumeCriteria = ProcessCriteria.builder().copy(processCriteria).lifecycles(
        LifeCycleStatus.SUSPENDED
    ).build();
    Iterator<Process> procs = processes.getProcesses(resumeCriteria).iterator();
    ResumeTask resume;
    Process proc;

    DistributionDatabase store = lookup(DistributionDatabase.class);

    Distribution dist;
    ProcessConfig conf;
    int restartCount = 0;

    while (procs.hasNext()) {
      proc = (Process) procs.next();
      DistributionCriteria criteria = DistributionCriteria.builder().name(proc.getDistributionInfo().getName())
          .version(proc.getDistributionInfo().getVersion()).build();
      try {
        dist = store.getDistribution(criteria);
      } catch (DistributionNotFoundException e) {
        store.removeDistribution(criteria);
        continue;
      }

      conf = dist.getProcess(proc.getDistributionInfo().getProcessName());

      if (conf == null) {
        ProgressQueue q = new ProgressQueueImpl();
        q.error("No process named '" + proc.getDistributionInfo().getProcessName() + "'");
        q.close();

        return q;
      }

      proc.touch();
      proc.save();

      resume = new ResumeTask();
      taskman.execute(resume, TaskParams.createFor(proc, dist, conf));
      restartCount++;
    }

    ProgressQueue q = new ProgressQueueImpl();

    if (restartCount == 0) {
      q.warning("No suspended processes to restart.");
      q.close();
    } else {
      q.warning("Restarted " + restartCount + " suspended processes.");
      q.close();
    }

    return q;
  }

  @Override
  public Process getProcess(String corusPid) throws ProcessNotFoundException {
    return this.processes.getProcess(corusPid);
  }

  @Override
  public List<Process> getProcesses(ProcessCriteria criteria) {
    List<Process> toReturn =  processes.getProcesses(criteria);
    return toReturn;
  }

  @Override
  public List<Process> getProcessesWithPorts() {
    List<Process> toReturn = new ArrayList<Process>();
    List<Process> processes = this.processes.getProcesses();
    for (int i = 0; i < processes.size(); i++) {
      Process p = processes.get(i);
      if (p.getActivePorts().size() > 0) {
        toReturn.add(p);
      }
    }
    return toReturn;
  }

  @Override
  public List<Status> getStatus(ProcessCriteria criteria) {
    return copyStatus(getProcesses(criteria));
  }

  @Override
  public ProcStatus getStatusFor(String corusPid) throws ProcessNotFoundException {
    Process proc = getProcess(corusPid);

    return copyStatus(proc);
  }
  
  @Override
  public void clean() {
    ProcessCriteria criteria = ProcessCriteria.builder().lifecycles(
        LifeCycleStatus.SUSPENDED, 
        LifeCycleStatus.STALE, 
        LifeCycleStatus.KILL_CONFIRMED,
        LifeCycleStatus.KILL_REQUESTED
    ).build();
    
    List<Process> toClean = processes.getProcesses(criteria);
    
    for (Process p : toClean) {
      // trying to kill stale process, just it case it is zombie
      if (p.getStatus() == LifeCycleStatus.STALE || p.getStatus() == LifeCycleStatus.KILL_REQUESTED) {
        try {
          os.killProcess(new OsModule.LogCallback() {
            @Override
            public void error(String error) {
            }
            @Override
            public void debug(String msg) {
            }
          }, KillSignal.SIGKILL, p.getOsPid());
        } catch (IOException e) {
          // noop
        }
      }
      processes.removeProcess(p.getProcessID());
      p.releasePorts(portManager);
    }
  }
  
  @Override
  public void archiveExecConfigs(RevId revId) {
    execConfigs.archiveExecConfigs(revId);
  }
  
  @Override
  public void unarchiveExecConfigs(RevId revId) {
    execConfigs.unarchiveExecConfigs(revId);
  }

  private List<Status> copyStatus(List<Process> processes) {
    List<Status> stat = new ArrayList<Status>(processes.size());
    for (Process p : processes) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Returning status %s", p.getProcessStatus()));
      }
      stat.add(copyStatus(p));
    }

    return stat;
  }

  private ProcStatus copyStatus(Process p) {
    return new ProcStatus(p);
  }
  
  /**
   * Internal method that handles property change events.
   * 
   * @param event The property change event to process.
   */
  protected void doHandlePropertyChangeEvent(PropertyChangeEvent event) {
    // Handle any server related property change
    if (PropertyScope.SERVER == event.getScope()) {
      if (event.containsProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED)) {
        Property property = event.getFirstPropertyFor(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED);
        if (EventType.ADD == event.getEventType()) {
          log.debug("Changing process configuration update to value: " + property.getValue());
          isPublishProcessConfigurationChangeEnabled = Boolean.parseBoolean(property.getValue());
        } else if (EventType.REMOVE == event.getEventType()) {
          log.debug("Reverting process configuration update to base value (corus.properties file)");
          isPublishProcessConfigurationChangeEnabled = Boolean.parseBoolean(
              serverContext().getCorusProperties().getProperty(CorusConsts.PROPERTY_CORUS_PROCESS_CONFIG_UPDATE_ENABLED, "false"));
        }
        log.info("Process configuration update functionality is now " + (isPublishProcessConfigurationChangeEnabled? "activated": "disabled"));
      }
    }
    
    // Publication of configuration changes to processes (if enabled)
    if (PropertyScope.PROCESS == event.getScope() && isPublishProcessConfigurationChangeEnabled) {
      List<Property> updatedProps = new ArrayList<>();
      if (EventType.ADD == event.getEventType()) {
        updatedProps.addAll(event.getProperties());
      }
      
      List<Property> deletedProps = new ArrayList<>();
      if (EventType.REMOVE == event.getEventType()) {
        deletedProps.addAll(event.getProperties());
      }
      
      taskman.execute(new PublishConfigurationChangeTask(),
          TaskParams.createFor(updatedProps, deletedProps));
    }
  }

  public class ProcessorInterceptor implements Interceptor {
    public void onUndeploymentEvent(UndeploymentEvent evt) {
      execConfigs.removeProcessesForDistribution(evt.getDistribution());
    }
  }
  
  public class PropertyChangeInterceptor implements Interceptor {
    public void onPropertyChangeEvent(PropertyChangeEvent event) {
      doHandlePropertyChangeEvent(event);
    }
  }
  
}
