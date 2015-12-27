package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.ubik.util.Assertions;

/**
 * Holds diagnostic results for process instances corresponding to a given 
 * process configuration.
 * 
 * @author yduchesne
 *
 */
public class ProcessConfigDiagnosticResult implements Externalizable, JsonStreamable {

  public static final class Builder {
    
    private ProcessConfigDiagnosticStatus status;
    private Distribution                  distribution;
    private ProcessConfig                 processConfig;
    private List<ProcessDiagnosticResult> processResults = new ArrayList<ProcessDiagnosticResult>();
    
    public Builder distribution(Distribution distribution) {
      this.distribution = distribution;
      return this;
    }
    
    public Builder processConfig(ProcessConfig config) {
      this.processConfig = config;
      return this;
    }
    
    /**
     * @param result a {@link ProcessConfigDiagnosticResult}.
     * @return this instance.
     */
    public Builder results(ProcessDiagnosticResult result) {
      processResults.add(result);
      return this;
    }
    
    /**
     * @param status a {@link ProcessConfigDiagnosticStatus}.
     * @return this instance.
     */
    public Builder status(ProcessConfigDiagnosticStatus status) {
      this.status = status;
      return this;
    }
    
    /**
     * @param result a list of {@link ProcessDiagnosticResult}s, corresponding to the diagnostics of each port
     * of a specific process instance.
     * @return this instance.
     */
    public Builder results(List<ProcessDiagnosticResult> results) {
      processResults.addAll(results);
      return this;
    }
    
    /**
     * @param the {@link ProcessConfigDiagnosticEnv} to use for accessing contextual data pertaining
     * diagnostic acquisition.
     * 
     * @return a new {@link ProcessConfigDiagnosticResult}, holdings this instance's data.
     */
    public ProcessConfigDiagnosticResult build(ProcessConfigDiagnosticEnv context) {
      Assertions.notNull(distribution, "Distribution config not set");
      Assertions.notNull(processConfig, "Process config not set");
      Assertions.illegalState(!processResults.isEmpty() && status != null, 
          "If process results are set, process config status should not be set explicitely (will be inferred from process status)");
      
      if (status != null) {
        // noop
        
      // no processes were found
      } else if (processResults.isEmpty()) {
        if (context.getExpectedInstanceCount() == 0) {
          status = ProcessConfigDiagnosticStatus.NO_PROCESSES_EXPECTED;
        } else if (context.isWithinGracePeriod()) {
          status = ProcessConfigDiagnosticStatus.PENDING_EXECUTION;
        } else {
          status = ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES;
        }
      // at least one process was found
      } else {
        
        Collections.sort(processResults, new Comparator<ProcessDiagnosticResult>() {
          @Override
          public int compare(ProcessDiagnosticResult r1, ProcessDiagnosticResult r2) {
            if (r1.getStatus().isProblem() && !r2.getStatus().isProblem()) {
              return -1;
            } else if (!r1.getStatus().isProblem() && r2.getStatus().isProblem()) {
              return 1;
            } else {
              if (!r1.getStatus().isFinal() && r2.getStatus().isFinal()) {
                return -1;
              } else if (r1.getStatus().isFinal() && !r2.getStatus().isFinal()) {
                return 1;
              } else {
                return 0;
              }
            }
          }
        });
          
        ProcessDiagnosticResult firstResult = processResults.get(0);
        
        // not all expected processes were found
        if (processResults.size() < context.getExpectedInstanceCount()) {
          if (firstResult.getStatus().isFinal() && firstResult.getStatus().isProblem()) {
            status = ProcessConfigDiagnosticStatus.FAILURE;
          } else if (context.isWithinGracePeriod()) {
            status = ProcessConfigDiagnosticStatus.PENDING_EXECUTION;
          } else {
            status = ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES;
          }
          
        // all expected processes were found
        } else {
          if (firstResult.getStatus().isProblem()) {
            status = ProcessConfigDiagnosticStatus.FAILURE;
          } else if (isDiagnosticUnavailable()) {
            status = ProcessConfigDiagnosticStatus.NO_DIAGNOSTIC_AVAILABLE;
          } else if (firstResult.getStatus().isFinal()) {
            status = ProcessConfigDiagnosticStatus.SUCCESS;
          } else if (context.isGracePeriodExhausted()) {
            status = ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES;
          } else {
            status = ProcessConfigDiagnosticStatus.PENDING_EXECUTION;
          }
        }
      }
      
      SuggestionDiagnosticAction action;
      if (status == ProcessConfigDiagnosticStatus.NO_DIAGNOSTIC_AVAILABLE) {
        action = SuggestionDiagnosticAction.REMEDIATE;
      } else if (status.isProblem() && status.isFinal()) {
        action = SuggestionDiagnosticAction.REMEDIATE;
      } else if (!status.isProblem() && status.isFinal()) {
        action = SuggestionDiagnosticAction.NOOP;
      } else if (!status.isFinal()) {
        action = SuggestionDiagnosticAction.RETRY;
      } else {
        action = SuggestionDiagnosticAction.NOOP;
      }
      
      return new ProcessConfigDiagnosticResult(action, status, distribution, processConfig, processResults);
    }
  
    private boolean isDiagnosticUnavailable() {
      
      int noDiagnosticCount = 0;
      for (ProcessDiagnosticResult r : processResults) {
        if (r.getStatus() == ProcessDiagnosticStatus.NO_DIAGNOSTIC_CONFIG) {
          noDiagnosticCount++;
        }
      }
      return noDiagnosticCount == processResults.size();
    }
  
    /**
     * @return a new instance of this class.
     */
    public static final Builder newInstance() {
      return new Builder();
    }
    
  }
  
  // ==========================================================================
 
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
    
  private SuggestionDiagnosticAction    suggestedAction;
  private ProcessConfigDiagnosticStatus status;
  private Distribution                  distribution;
  private ProcessConfig                 processConfig;
  private List<ProcessDiagnosticResult> processResults = new ArrayList<ProcessDiagnosticResult>();
  
  public ProcessConfigDiagnosticResult() {
  }
  
  public ProcessConfigDiagnosticResult(
      SuggestionDiagnosticAction suggestedAction,
      ProcessConfigDiagnosticStatus status, 
      Distribution dist,
      ProcessConfig config, 
      List<ProcessDiagnosticResult> results) {
    this.suggestedAction = suggestedAction;
    this.status          = status;
    this.distribution    = dist;
    this.processConfig   = config;
    this.processResults  = results;
  }
  
  public ProcessConfigDiagnosticStatus getStatus() {
    return status;
  }
  
  public SuggestionDiagnosticAction getSuggestedAction() {
    return suggestedAction;
  }
  
  public Distribution getDistribution() {
    return distribution;
  }
  
  public ProcessConfig getProcessConfig() {
    return processConfig;
  }
  
  void setProcessConfig(ProcessConfig processConfig) {
    this.processConfig = processConfig;
  }
  
  public List<ProcessDiagnosticResult> getProcessResults() {
    return processResults;
  }
  
  void addProcessDiagnosticResult(ProcessDiagnosticResult result) {
    processResults.add(result);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(status, distribution, processConfig);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessConfigDiagnosticResult) {
      ProcessConfigDiagnosticResult other = (ProcessConfigDiagnosticResult) obj;
      return ObjectUtil.safeEquals(status, other.status) 
          && ObjectUtil.safeEquals(distribution, other.distribution) 
          && ObjectUtil.safeEquals(processConfig, other.processConfig)
          && ObjectUtil.safeListEquals(processResults, other.processResults);
    }
    return false;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable interface
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("status").value(status.name())
      .field("suggestedAction").value(suggestedAction.name())
      .field("name").value(processConfig.getName())
      .field("distribution");
    distribution.toJson(stream, level);      
    if (level.greaterThan(ContentLevel.MINIMAL)) {
      stream.field("results").beginArray();
      for (ProcessDiagnosticResult r : processResults) {
        r.toJson(stream, level);
      }
      stream.endArray();
    }
    stream.endObject();
  }

  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    
    int inputVersion = in.readInt();
    
    if (inputVersion == VERSION_1) {
      suggestedAction = (SuggestionDiagnosticAction) in.readObject();
      status          = (ProcessConfigDiagnosticStatus) in.readObject();
      distribution    = (Distribution) in.readObject();
      processConfig   = (ProcessConfig) in.readObject();
      processResults  = (List<ProcessDiagnosticResult>) in.readObject();
            
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
    
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);

    out.writeObject(suggestedAction);
    out.writeObject(status);
    out.writeObject(distribution);
    out.writeObject(processConfig);
    out.writeObject(processResults);
  }
  
}
