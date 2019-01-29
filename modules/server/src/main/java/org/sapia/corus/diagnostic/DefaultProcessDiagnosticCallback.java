package org.sapia.corus.diagnostic;

import java.util.ArrayList;
import java.util.List;

import org.apache.log.Logger;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.diagnostic.evaluator.ProcessConfigDiagnosticEvaluationContext;

/**
 * Implements the default behavior for actually performing process diagnostic acquisition.
 * 
 * @author yduchesne
 *
 */
class DefaultProcessDiagnosticCallback implements ProcessDiagnosticCallback {

  private Logger        log;
  private ServerContext serverContext;
  private LockOwner     lockOwner = LockOwner.createInstance();
  private List<? extends ProcessDiagnosticProvider> diagnosticProviders;
  
  DefaultProcessDiagnosticCallback(
      Logger log, 
      ServerContext context, 
      List<? extends ProcessDiagnosticProvider> providers) {
    this.log                 = log;
    this.serverContext       = context;
    this.diagnosticProviders = providers;
  }

  @Override
  public List<ProcessDiagnosticResult> invoke(
      ProcessConfigDiagnosticEvaluationContext context, Process toDiagnose) {
    List<ProcessDiagnosticResult> results = new ArrayList<ProcessDiagnosticResult>();
    try {
      if (log.isDebugEnabled()) log.debug("Performing diagnostic acquisition for process " + toDiagnose);
      if (context.getLockOwner().isSet()) {
        toDiagnose.getLock().acquire(context.getLockOwner().get());
      } else {
        toDiagnose.getLock().acquire(lockOwner);
      }
      if (!toDiagnose.getActivePorts().isEmpty()) {
        for (ActivePort activePort : toDiagnose.getActivePorts()) {
          OptionalValue<Port> portRange = context.getProcessConfig().getPortByName(activePort.getName());
          if (portRange.isSet()) {
            if (portRange.get().getDiagnosticConfig().isSet()) {
              DiagnosticContext diagContext = new DiagnosticContext(toDiagnose, activePort, portRange.get().getDiagnosticConfig().get() , serverContext);
              ProcessDiagnosticProvider provider = selectDiagnosticProvider(diagContext);
              ProcessDiagnosticResult result = provider.performDiagnostic(diagContext); 
              results.add(result);
            } else {
              String msg = String.format("Process %s (%s) has no diagnostic config defined for port %s", 
                  ToStringUtil.toString(toDiagnose), ToStringUtil.toString(context.getDistribution(), context.getProcessConfig()), activePort.getName());
              log.debug(msg);
              OptionalValue<String> noneProtocol = OptionalValue.none();
              results.add(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG, msg, toDiagnose, noneProtocol, activePort));
            }
          } else {
            throw new IllegalStateException("No port range defined for given port range name: " + activePort.getName());
          }
        }
      } else {
        String msg = String.format(
            "Process %s (%s) has no active ports", 
            ToStringUtil.toString(toDiagnose), ToStringUtil.toString(context.getDistribution(), context.getProcessConfig())
        );
        results.add(new ProcessDiagnosticResult(ProcessDiagnosticStatus.NO_ACTIVE_PORT, msg, toDiagnose));
      }
    } catch (ProcessLockException ple) {
      String msg = String.format(
          "Process %s (%s) is currently locked", 
          ToStringUtil.toString(toDiagnose), ToStringUtil.toString(context.getDistribution(), context.getProcessConfig())
      );
      results.add(new ProcessDiagnosticResult(ProcessDiagnosticStatus.PROCESS_LOCKED, msg, toDiagnose));
    } finally {
      if (context.getLockOwner().isNull()) {
        toDiagnose.getLock().release(lockOwner);
      } else {
        toDiagnose.getLock().release(context.getLockOwner().get());
      }
    }
    
    return results;
  }

  private ProcessDiagnosticProvider selectDiagnosticProvider(DiagnosticContext context) {
    for (ProcessDiagnosticProvider provider : diagnosticProviders) {
      if (provider.accepts(context)) {
        return provider;
      }
    } 
    throw new IllegalStateException("No process diagnostic provider found for: " + context.getDiagnosticConfig().getProtocol());
  }
}
