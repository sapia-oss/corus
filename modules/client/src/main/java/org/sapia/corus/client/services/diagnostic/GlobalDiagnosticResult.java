package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;

/**
 * Holds global diagnostic data.
 * 
 * @author yduchesne
 *
 */
public class GlobalDiagnosticResult implements Externalizable, JsonStreamable {

  public static class Builder {
    
    private List<ProcessConfigDiagnosticResult>     processResults  = Collections.emptyList();
    private OptionalValue<ProgressDiagnosticResult> progressResult  = OptionalValue.none();
    private List<SystemDiagnosticResult>            systemResults   = Collections.emptyList();
    
    private Builder() {
    }
    
    public static Builder newInstance() {
      return new Builder();
    }
    
    public Builder processDiagnostics(List<ProcessConfigDiagnosticResult> results) {
      this.processResults = results;
      return this;
    }
    
    public Builder progressDiagnostics(ProgressDiagnosticResult result) {
      this.progressResult = OptionalValue.of(result);
      return this;
    }
    
    public Builder systemDiagnostics(List<SystemDiagnosticResult> result) {
      this.systemResults = result;
      return this;
    }
    
    public GlobalDiagnosticResult build() {
      GlobalDiagnosticStatus status = determineGlobalDiagnosticFromSystemResults();
      if (status == GlobalDiagnosticStatus.INCOMPLETE) {
        // noop
      } else if (progressResult.isSet() && !progressResult.get().getErrorMessages().isEmpty()) {
        status = GlobalDiagnosticStatus.FAILURE;
      } else {
        for (ProcessConfigDiagnosticResult r : processResults) {
          if (r.getStatus().isFinal() && r.getStatus().isProblem()) {
            status = GlobalDiagnosticStatus.FAILURE;
            break;
          } else if (r.getSuggestedAction() == SuggestionDiagnosticAction.RETRY) {
            status = GlobalDiagnosticStatus.INCOMPLETE;
            break;
          }
        }
      }
      return new GlobalDiagnosticResult(status, systemResults, processResults, progressResult);
    }
    
    private GlobalDiagnosticStatus determineGlobalDiagnosticFromSystemResults() {
      int incompleteCount = 0;
      int failureCount    = 0;
      
      for(SystemDiagnosticResult r : systemResults) {
         switch (r.getStatus().getMatchingGlobalDiagnostic()) {
           case FAILURE:
             failureCount++;
             break;
           case INCOMPLETE:
             incompleteCount++;
             break;
           case SUCCESS:
             // noop
             break;
           default:
             throw new IllegalStateException("State not handled: " + r.getStatus().getMatchingGlobalDiagnostic());
         }
      }
      if (failureCount > 0) {
        return GlobalDiagnosticStatus.FAILURE;
      }
      if (incompleteCount > 0) {
        return GlobalDiagnosticStatus.INCOMPLETE;
      }
      return GlobalDiagnosticStatus.SUCCESS;
    }
    
  }
  
  // ==========================================================================
  
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private GlobalDiagnosticStatus                  status;
  private List<SystemDiagnosticResult>            systemDiagnosticResults;
  private List<ProcessConfigDiagnosticResult>     processResults;
  private OptionalValue<ProgressDiagnosticResult> progressResult;
  
  /**
   * DO NOT CALL: serialization only
   */
  public GlobalDiagnosticResult() {
  }
  
  public GlobalDiagnosticResult(
      GlobalDiagnosticStatus                   status, 
      List<SystemDiagnosticResult>             systemDiagnosticResults,
      List<ProcessConfigDiagnosticResult>      processResults,
      OptionalValue<ProgressDiagnosticResult>  progressResult) {
    this.status                  = status;
    this.systemDiagnosticResults = systemDiagnosticResults;
    this.processResults          = processResults;
    this.progressResult          = progressResult;
  }
  
  public GlobalDiagnosticStatus getStatus() {
    return status;
  }
  
  public List<ProcessConfigDiagnosticResult> getProcessResults() {
    return processResults;
  }
  
  public List<SystemDiagnosticResult> getSystemDiagnosticResults() {
    return systemDiagnosticResults;
  }
  
  public OptionalValue<ProgressDiagnosticResult> getProgressResult() {
    return progressResult;
  }

  // ------------------------------------------------------
  // JsonStreamble
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("status").value(status.name())
      .field("systemDiagnostics").beginArray();
    for (SystemDiagnosticResult r : this.systemDiagnosticResults) {
      r.toJson(stream, level);
    }
    stream.endArray();
   
    stream.field("processDiagnostics").beginArray();
    for (ProcessConfigDiagnosticResult r : processResults) {
      r.toJson(stream, level);
    }
    stream.endArray();
    
    stream.field("progressDiagnostics");
    if (progressResult.isSet()) {
      progressResult.get().toJson(stream, level);
    }
    
    stream.endObject();
  }
  
  // ------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      status                  = (GlobalDiagnosticStatus) in.readObject();
      systemDiagnosticResults = (List<SystemDiagnosticResult>) in.readObject();
      processResults          = (List<ProcessConfigDiagnosticResult>) in.readObject();
      progressResult          = (OptionalValue<ProgressDiagnosticResult>) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeObject(status);
    out.writeObject(systemDiagnosticResults);
    out.writeObject(processResults);
    out.writeObject(progressResult);
  }
}
