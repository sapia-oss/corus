package org.sapia.corus.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.GlobalDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.diagnostic.ProgressDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.SuggestionDiagnosticAction;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.event.ProcessStartPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessStartedEvent;
import org.sapia.corus.client.services.repository.Repository;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.diagnostic.evaluator.AllProcessesRunningEvaluator;
import org.sapia.corus.diagnostic.evaluator.NoProcessGracePeriodExhaustedEvaluator;
import org.sapia.corus.diagnostic.evaluator.NoProcessWithinGracePeriodEvaluator;
import org.sapia.corus.diagnostic.evaluator.NotAllProcessesRunningGracePeriodExhaustedEvaluator;
import org.sapia.corus.diagnostic.evaluator.NotAllProcessesRunningWithinGracePeriodEvaluator;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluationContext;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluator;
import org.sapia.corus.diagnostic.provider.HttpProcessDiagnosticProvider;
import org.sapia.corus.taskmanager.CorusTaskManager;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SysClock;
import org.springframework.beans.factory.annotation.Autowired;

@Bind(moduleInterface = { DiagnosticModule.class })
@Remote(interfaces = DiagnosticModule.class)
public class DiagnosticModuleImpl extends ModuleHelper implements  DiagnosticModule, Interceptor {
  
  class PendingProcessInfo {
    
    private ProcessStartupInfo startupInfo;
    private int                currentlyStarted = 0;
    private ProcessConfig      processConfig;
  
    int getCurrentlyStarted() {
      return currentlyStarted;
    }
    
    ProcessConfig getProcessConfig() {
      return processConfig;
    }
    
    ProcessStartupInfo getStartupInfo() {
      return startupInfo;
    }
    
    public PendingProcessInfo(ProcessConfig conf, ProcessStartupInfo startupInfo) {
      this.processConfig = conf;
      this.startupInfo   = startupInfo;
    }
  }
  
  // ==========================================================================
  
  public static final int DEFAULT_GRACE_PERIOD_DURATION_SECONDS = 60;
  
  @Autowired
  private Deployer   deployer;
  
  @Autowired
  private Repository repository;
  
  @Autowired
  private Processor  processes;

  @Autowired
  private InternalConfigurator configurator;
  
  @Autowired
  private EventDispatcher dispatcher;
  
  @Autowired
  private CorusTaskManager taskManager;
 
  LockOwner lockOwner = LockOwner.createInstance();
  
  private long startTime = System.currentTimeMillis();
  private volatile long lastProgressCheck;
  
  private SysClock clock = SysClock.RealtimeClock.getInstance();
  
  private volatile DynamicProperty<Integer>         processStartupGracePeriodDuration = 
      new DynamicProperty<Integer>(DEFAULT_GRACE_PERIOD_DURATION_SECONDS);
  
  private List<? extends ProcessDiagnosticProvider>        diagnosticProviders = 
      Collects.arrayToList(new HttpProcessDiagnosticProvider());
  
  private List<? extends ProcessConfigDiagnosticEvaluator> diagnosticEvaluators = 
      Collects.arrayToList(
          new NoProcessGracePeriodExhaustedEvaluator(), 
          new NoProcessWithinGracePeriodEvaluator(), 
          new NotAllProcessesRunningGracePeriodExhaustedEvaluator(),
          new NotAllProcessesRunningWithinGracePeriodEvaluator(), 
          new AllProcessesRunningEvaluator()
  );
  
  private ProcessDiagnosticCallback diagnosticCallback;
  
  private Map<String, PendingProcessInfo>           pendingProcessInfoByGroup = 
      Collections.synchronizedMap(new HashMap<String, PendingProcessInfo>());

  // --------------------------------------------------------------------------
  // Provided for testing purposes

  void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }
  
  void setProcesses(Processor processes) {
    this.processes = processes;
  }
  
  void setDispatcher(EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }
  
  void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }
  
  void setStartTime(long startTime) {
    this.startTime = startTime;
  }
  
  void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  void setDiagnosticProviders(List<? extends ProcessDiagnosticProvider> diagnosticProviders) {
    this.diagnosticProviders = diagnosticProviders;
  }
  
  void setDiagnosticEvaluators(List<? extends ProcessConfigDiagnosticEvaluator> diagnosticEvaluators) {
    this.diagnosticEvaluators = diagnosticEvaluators;
  }
  
  void setRepository(Repository repository) {
    this.repository = repository;
  }
  
  void setTaskManager(CorusTaskManager taskManager) {
    this.taskManager = taskManager;
  }
  
  Map<String, PendingProcessInfo> getPendingProcessInfoByGroup() {
    return pendingProcessInfoByGroup;
  }
 
  // --------------------------------------------------------------------------
  // Config setters
  
  public void setProcessStartupGracePeriodDuration(int processStartupGracePeriodDurationSecs) {
    this.processStartupGracePeriodDuration = new DynamicProperty<Integer>(processStartupGracePeriodDurationSecs);
  }
 
  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return DiagnosticModule.ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    
    diagnosticCallback = new DefaultProcessDiagnosticCallback(log, serverContext(), diagnosticProviders);
    
    processStartupGracePeriodDuration.addListener(new DynamicProperty.DynamicPropertyListener<Integer>() {
      @Override
      public void onModified(DynamicProperty<Integer> updated) {
        processStartupGracePeriodDuration = updated;
      }
    });
    
    
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DIAGNOSTIC_GRACE_PERIOD_DURATION, processStartupGracePeriodDuration);
    
    dispatcher.addInterceptor(ProcessStartPendingEvent.class, this);
    dispatcher.addInterceptor(ProcessStartedEvent.class, this);
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // Event interception methods
  
  public void onProcessStartPendingEvent(ProcessStartPendingEvent event) {
    log.info(String.format(
        "Got %s processes pending execution for %s", 
        event.getStartupInfo().getRequestedInstances(), 
        ToStringUtils.toString(event.getDistribution(), event.getProcess())
    ));
    
    pendingProcessInfoByGroup.put(
        event.getStartupInfo().getStartGroupId(), 
        new PendingProcessInfo(event.getProcess(), event.getStartupInfo())
    );
  }
  
  public void onProcessStartedEvent(ProcessStartedEvent event) {
    PendingProcessInfo info = pendingProcessInfoByGroup.get(event.getProcess().getStartupInfo().getStartGroupId());
    if (info == null) {
      log.warn("Abnormal situation: no info found for process startup group: " + event.getProcess().getStartupInfo().getStartGroupId());
    } else {
      info.currentlyStarted++;
      if (info.currentlyStarted >= info.startupInfo.getRequestedInstances()) {
        pendingProcessInfoByGroup.remove(info.startupInfo.getStartGroupId());
        if (log.isDebugEnabled()) { 
            log.debug(String.format(
                "All expected processes started for %s (startup group = %s)", 
                ToStringUtils.toString(event.getDistribution(), event.getProcessConfig()), info.startupInfo.getStartGroupId())
            );
        }
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // DiagnosticModule interface
  
  @Override
  public synchronized GlobalDiagnosticResult acquireDiagnostics() {
    if (deployer.getState().get().isBusy() || repository.getState().get().isBusy()|| processes.getState().get().isBusy()) {
      return GlobalDiagnosticResult.Builder.newInstance().busy().build();
    } else {
      GlobalDiagnosticResult.Builder builder = GlobalDiagnosticResult.Builder.newInstance();
      List<ProgressMsg>        lastProgressMsgs = taskManager.clearBufferedMessages(ProgressMsg.ERROR, lastProgressCheck);
      ProgressDiagnosticResult progressResult   = new ProgressDiagnosticResult(Collects.convertAsList(lastProgressMsgs, new Func<String, ProgressMsg>() {
        @Override
        public String call(ProgressMsg msg) {
          if (msg.isThrowable()) {
            return msg.getThrowable().getMessage();
          } 
          return msg.getMessage().toString();
        }
      }));
   
      builder.progressDiagnostics(progressResult);
      builder.processDiagnostics(acquireProcessDiagnostics());
      lastProgressCheck = clock.nanoTime();
      return builder.build();
    }
  }
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  List<ProcessConfigDiagnosticResult> acquireProcessDiagnostics() {
    List<ProcessConfigDiagnosticResult> allResults = new ArrayList<ProcessConfigDiagnosticResult>();
    
    for (Distribution dist : deployer.getDistributions(DistributionCriteria.builder().all())) {
      if (log.isDebugEnabled()) log.debug("Acquiring diagnostics for distribution: " + ToStringUtils.toString(dist));
      for (ProcessConfig pc : dist.getProcesses()) {
        if (log.isDebugEnabled()) log.debug("Checking for process config: " + ToStringUtils.toString(dist, pc));
        ProcessConfigDiagnosticResult.Builder diagnosticResult = ProcessConfigDiagnosticResult.Builder.newInstance()
            .distribution(dist)
            .processConfig(pc);
        Set<String> processTags = new HashSet<String>(pc.getTagSet());
        processTags.addAll(dist.getTagSet());
        Set<String> serverTags = Collects.convertAsSet(configurator.getTags(), new Func<String, Tag>() {
          @Override
          public String call(Tag t) {
            return t.getValue();
          }
        });
        
        List<Process> staleProcesses       = getProcessesFor(dist, pc, LifeCycleStatus.STALE);
        List<Process> restartingProcesses  = getProcessesFor(dist, pc, LifeCycleStatus.RESTARTING);
        List<Process> terminatingProcesses = getProcessesFor(dist, pc, LifeCycleStatus.KILL_CONFIRMED, LifeCycleStatus.KILL_REQUESTED);
        
        if (deployer.getState().get().isBusy() || repository.getState().get().isBusy()|| processes.getState().get().isBusy()) {
          if (log.isInfoEnabled()) log.info("System busy, cannot acquire diagnostic for: " + ToStringUtils.toString(dist, pc));
          
          ProcessConfigDiagnosticEvaluationContext evalContext = new ProcessConfigDiagnosticEvaluationContext(
              diagnosticCallback, 
              diagnosticResult, 
              dist, pc, new ArrayList<Process>(), 0
          )
          .withLog(log)
          .withClock(clock)
          .withGracePeriod(processStartupGracePeriodDuration.getValueNotNull())
          .withStartTime(startTime);
          
          
          allResults.add(
              ProcessConfigDiagnosticResult.Builder.newInstance()
                .status(ProcessConfigDiagnosticStatus.BUSY)
                .distribution(dist)
                .processConfig(pc)
                .build(evalContext)
          );
          
          return allResults;
          
        } else if (!staleProcesses.isEmpty()) { 
          log.warn(String.format("Got %s stale processes for %s", staleProcesses.size(), ToStringUtils.toString(dist, pc)));
         
          List<ProcessDiagnosticResult> pdrs = new ArrayList<ProcessDiagnosticResult>();
          for (Process p : staleProcesses) {
            ProcessDiagnosticResult pdr = new ProcessDiagnosticResult(ProcessDiagnosticStatus.STALE, "Process is stale", p);
            pdrs.add(pdr);
          }
          
          allResults.add(new ProcessConfigDiagnosticResult(
              SuggestionDiagnosticAction.REMEDIATE, ProcessConfigDiagnosticStatus.FAILURE, dist, pc, pdrs
          ));
          
        } else if (!terminatingProcesses.isEmpty()) {
          log.warn(String.format("Got %s terminating processes for %s", terminatingProcesses.size(), ToStringUtils.toString(dist, pc)));
          
          List<ProcessDiagnosticResult> pdrs = new ArrayList<ProcessDiagnosticResult>();
          for (Process p : staleProcesses) {
            ProcessDiagnosticResult pdr = new ProcessDiagnosticResult(ProcessDiagnosticStatus.SHUTTING_DOWN, "Process is shutting down", p);
            pdrs.add(pdr);
          }
          
          allResults.add(new ProcessConfigDiagnosticResult(
              SuggestionDiagnosticAction.RETRY, ProcessConfigDiagnosticStatus.BUSY, dist, pc, pdrs
          ));
          
        }  else if (!restartingProcesses.isEmpty()) {
          log.warn(String.format("Got %s restarting processes for %s", restartingProcesses.size(), ToStringUtils.toString(dist, pc)));
          
          List<ProcessDiagnosticResult> pdrs = new ArrayList<ProcessDiagnosticResult>();
          for (Process p : staleProcesses) {
            ProcessDiagnosticResult pdr = new ProcessDiagnosticResult(ProcessDiagnosticStatus.RESTARTING, "Process is retarting", p);
            pdrs.add(pdr);
          }
          
          allResults.add(new ProcessConfigDiagnosticResult(
              SuggestionDiagnosticAction.RETRY, ProcessConfigDiagnosticStatus.BUSY, dist, pc, pdrs
          ));
          
        } else if ((serverTags.isEmpty() && processTags.isEmpty()) || serverTags.containsAll(processTags)) {

          int expectedCount = getExpectedInstancesFor(pc);
          
          ProcessCriteria pCriteria = ProcessCriteria.builder()
              .distribution(dist.getName())
              .version(dist.getVersion())
              .name(pc.getName())
              .lifecycles(LifeCycleStatus.ACTIVE)
              .build();
          
          List<Process> toDiagnose = processes.getProcesses(pCriteria);
          
          if (log.isDebugEnabled()) 
            log.debug(String.format("Got %s processes currently running for %s", toDiagnose.size(), ToStringUtils.toString(dist, pc)));
          
          if (expectedCount < 0) {
            expectedCount = toDiagnose.size();
          }
          
          ProcessConfigDiagnosticEvaluationContext evalContext = new ProcessConfigDiagnosticEvaluationContext(
              diagnosticCallback, 
              diagnosticResult, 
              dist, pc, toDiagnose, expectedCount
          )
          .withLog(log)
          .withClock(clock)
          .withGracePeriod(processStartupGracePeriodDuration.getValueNotNull())
          .withStartTime(startTime);
          
          ProcessConfigDiagnosticEvaluator evaluator = selectEvaluator(evalContext);
          evaluator.evaluate(evalContext);
          
          allResults.add(diagnosticResult.build(evalContext));

        } else {
          if (log.isInfoEnabled()) log.info("No processes expected to run given non-matching tags for: " + ToStringUtils.toString(dist, pc));
          
          ProcessConfigDiagnosticEvaluationContext evalContext = new ProcessConfigDiagnosticEvaluationContext(
              diagnosticCallback, 
              diagnosticResult, 
              dist, pc, new ArrayList<Process>(), 0
          )
          .withLog(log)
          .withClock(clock)
          .withGracePeriod(processStartupGracePeriodDuration.getValueNotNull())
          .withStartTime(startTime);
          
          
          allResults.add(
              ProcessConfigDiagnosticResult.Builder.newInstance()
                .distribution(dist)
                .processConfig(pc)
                .build(evalContext)
          );
        }
      }
    }
    return allResults;
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods (visible for testing)
  
  int getExpectedInstancesFor(ProcessConfig config) {
    int toReturn = -1;
    synchronized (pendingProcessInfoByGroup) {
      for (Map.Entry<String, PendingProcessInfo> entry : pendingProcessInfoByGroup.entrySet()) {
        if (toReturn < 0) {
          toReturn = 0;
        }
        if (entry.getValue().processConfig.equals(config)) {
          toReturn += entry.getValue().startupInfo.getRequestedInstances();
        }
      }
    }
    return toReturn;
  }
  
  ProcessConfigDiagnosticEvaluator selectEvaluator(ProcessConfigDiagnosticEvaluationContext evalContext) {
    for (ProcessConfigDiagnosticEvaluator ev : diagnosticEvaluators) {
      if (ev.accepts(evalContext)) {
        return ev;
      }
    } 
    throw new IllegalStateException("No process config evaluator could be resolved");
  }

  private List<Process> getProcessesFor(Distribution dist, ProcessConfig pc, LifeCycleStatus...status) {
    ProcessCriteria criteria = ProcessCriteria.builder()
        .distribution(dist.getName())
        .version(dist.getVersion())
        .name(pc.getName())
        .lifecycles(status)
        .build();
    
    return processes.getProcesses(criteria);
  }
}
