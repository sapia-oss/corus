package org.sapia.corus.diagnostic.evaluator;

import java.util.List;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticEnv;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticResult.Builder;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.diagnostic.DiagnosticModuleImpl;
import org.sapia.corus.diagnostic.ProcessDiagnosticCallback;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Holds the state pertaining to diagnostic evaluation, in the context of process instances
 * corresponding to a given process configuration.
 */
public class ProcessConfigDiagnosticEvaluationContext implements ProcessConfigDiagnosticEnv {

  private Logger                                log         = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private long                                  startTime   = System.currentTimeMillis();
  private TimeValue                             gracePeriod = TimeValue.createSeconds(DiagnosticModuleImpl.DEFAULT_GRACE_PERIOD_DURATION_SECONDS);
  private SysClock                              clock       = SysClock.RealtimeClock.getInstance();
  private ProcessDiagnosticCallback             diagnosticCallback;
  private ProcessConfigDiagnosticResult.Builder results;
  private Distribution                          distribution;
  private ProcessConfig                         processConfig;
  private List<Process>                         toDiagnose;
  private int                                   expectedInstanceCount;
  
  public ProcessConfigDiagnosticEvaluationContext(
      ProcessDiagnosticCallback diagnosticCallback,
      ProcessConfigDiagnosticResult.Builder results,
      Distribution  distribution,
      ProcessConfig conf,
      List<Process> toDiagnose,
      int expected) {
    this.diagnosticCallback    = diagnosticCallback;
    this.results               = results;
    this.distribution          = distribution;
    this.processConfig         = conf;
    this.toDiagnose            = toDiagnose;
    this.expectedInstanceCount = expected;
  }
  
  /**
   * @return the {@link Logger} to use.
   */
  Logger getLog() {
    return log;
  }
  
  /**
   * @return the {@link Builder} to user for accumulating {@link ProcessDiagnosticResult}s.
   */
  public ProcessConfigDiagnosticResult.Builder getResultsBuilder() {
    return results;
  }
  
  /**
   * @return the {@link ProcessDiagnosticCallback}
   */
  public ProcessDiagnosticCallback getDiagnosticCallback() {
    return diagnosticCallback;
  }
  
  /**
   * @return the {@link SysClock} to use for obtaining the current time.
   */
  public SysClock getClock() {
    return clock;
  }
  
  /**
   * @return the {@link TimeValue} corresponding to the grace period duration to use.
   */
  public TimeValue getGracePeriod() {
    return gracePeriod;
  }
  
  /**
   * @return the time (in millis) at which Corus started.
   */
  public long getStartTime() {
    return startTime;
  }
  
  // --------------------------------------------------------------------------
  // ProcessConfigDiagnosticEnv interface
  
  /**
   * @return the {@link Distribution} in the context of which the given processes are being evaluated.
   */
  @Override
  public Distribution getDistribution() {
    return distribution;
  }
  
  /**
   * @return the {@link ProcessConfig} in the context of which the given processes are being evaluated.
   */
  @Override
  public ProcessConfig getProcessConfig() {
    return processConfig;
  }
  
  /**
   * @return the {@link List} of processes to diagnose.
   */
  @Override
  public List<Process> getProcesses() {
    return toDiagnose;
  }
  
  /**
   * @return the number of processes that are expected.
   */
  @Override
  public int getExpectedInstanceCount() {
    return expectedInstanceCount;
  }
  
  /**
   * @return <code>true</code> if the grace period is not yet exhausted.
   */
  @Override
  public boolean isWithinGracePeriod() {
    return clock.currentTimeMillis() - startTime <= gracePeriod.getValueInMillis();
  }
  
  /**
   * @return <code>true</code> if the grace period has exhausted.
   */
  @Override
  public boolean isGracePeriodExhausted() {
    return !isWithinGracePeriod();
  }
  
  // ------------------------------------------------------------------------
  // Config override methods
  
  /**
   * @param clock the {@link SysClock} instance to use.
   * @return this instance.
   */
  public ProcessConfigDiagnosticEvaluationContext withClock(SysClock clock) {
    this.clock = clock;
    return this;
  }
  
  /**
   * @param seconds the grace period to use, in seconds.
   * @return this instance.
   */
  public ProcessConfigDiagnosticEvaluationContext withGracePeriod(int seconds) {
    gracePeriod = TimeValue.createSeconds(seconds);
    return this;
  }
  
  /**
   * @param millis the number of milliseconds corresponding to the time at which Corus started. 
   * @return this instance.
   */
  public ProcessConfigDiagnosticEvaluationContext withStartTime(long millis) {
    this.startTime = millis;
    return this;
  }
  
  /**
   * @param log the {@link Logger} to use.
   * @return this instance.
   */
  public ProcessConfigDiagnosticEvaluationContext withLog(Logger log) {
    this.log = log;
    return this;
  }
  
}