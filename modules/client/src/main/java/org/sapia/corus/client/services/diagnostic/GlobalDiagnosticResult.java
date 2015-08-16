package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.util.Assertions;

/**
 * Holds global diagnostic data.
 * 
 * @author yduchesne
 *
 */
public class GlobalDiagnosticResult implements Externalizable, JsonStreamable {

  public static class Builder {
    
    private List<ProcessConfigDiagnosticResult> processResults;
    private ProgressDiagnosticResult            progressResult;
    private boolean                             busy;
    
    private Builder() {
    }
    
    public Builder busy() {
      this.busy = true;
      this.processResults = new ArrayList<ProcessConfigDiagnosticResult>();
      this.progressResult = new ProgressDiagnosticResult(new ArrayList<String>());
      return this;
    }
    
    public Builder processDiagnostics(List<ProcessConfigDiagnosticResult> results) {
      this.processResults = results;
      return this;
    }
    
    public Builder progressDiagnostics(ProgressDiagnosticResult result) {
      this.progressResult = result;
      return this;
    }
    
    public GlobalDiagnosticResult build() {
      Assertions.notNull(processResults, "Process diagnostic results not set");
      Assertions.notNull(progressResult, "Progress diagnostic results not set");

      GlobalDiagnosticStatus      status = null;
      if (busy) {
        status = GlobalDiagnosticStatus.INCOMPLETE;
      } else if (!progressResult.getErrorMessages().isEmpty()) {
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
      if (status == null) {
        status = GlobalDiagnosticStatus.SUCCESS;
      }
      return new GlobalDiagnosticResult(status, processResults, progressResult);
    }
    
    public static Builder newInstance() {
      return new Builder();
    }
  }
  
  // ==========================================================================
  
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private GlobalDiagnosticStatus              status;
  private List<ProcessConfigDiagnosticResult> processResults;
  private ProgressDiagnosticResult            progressResult;
  
  /**
   * DO NOT CALL: serialization only
   */
  public GlobalDiagnosticResult() {
  }
  
  public GlobalDiagnosticResult(
      GlobalDiagnosticStatus              status, 
      List<ProcessConfigDiagnosticResult> processResults,
      ProgressDiagnosticResult            progressResult) {
    this.status         = status;
    this.processResults = processResults;
    this.progressResult = progressResult;
  }
  
  public GlobalDiagnosticStatus getStatus() {
    return status;
  }
  
  public List<ProcessConfigDiagnosticResult> getProcessResults() {
    return processResults;
  }
  
  public ProgressDiagnosticResult getProgressResult() {
    return progressResult;
  }

  // ------------------------------------------------------
  // JsonStreamble
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("status").value(status.name())
      .field("processDiagnostics").beginArray();
    for (ProcessConfigDiagnosticResult r : processResults) {
      r.toJson(stream, level);
    }
    stream.endArray();
    stream.field("progressDiagnostics");
    progressResult.toJson(stream, level);
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
      status         = (GlobalDiagnosticStatus) in.readObject();
      processResults = (List<ProcessConfigDiagnosticResult>) in.readObject();
      progressResult = (ProgressDiagnosticResult) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeObject(status);
    out.writeObject(processResults);
    out.writeObject(progressResult);
  }
}
