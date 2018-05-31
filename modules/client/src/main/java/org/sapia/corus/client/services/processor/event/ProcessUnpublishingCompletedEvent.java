package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Strings;

/**
 * Dispatched when a process' unpublishing has completed.
 * 
 * @author yduchesne
 *
 */
public class ProcessUnpublishingCompletedEvent {
  
  public enum UnpublishStatus {
    NOT_APPLICABLE,
    SUCCESS,
    FAILURE
  }
  
  // ==========================================================================
 
  private Process                  process;
  private UnpublishStatus          status;
  private OptionalValue<Exception> error = OptionalValue.none();
  
  public ProcessUnpublishingCompletedEvent(Process process, Exception error) {
    this.process = process;
    this.status  = UnpublishStatus.FAILURE;
    this.error   = OptionalValue.of(error);
  }
  
  public ProcessUnpublishingCompletedEvent(Process process, UnpublishStatus status) {
    this.process = process;
    this.status  = status;
  }
  
  public Process getProcess() {
    return process;
  }

  public UnpublishStatus getStatus() {
    return status;
  }
  
  public OptionalValue<Exception> getError() {
    return error;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(process, status);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessUnpublishingCompletedEvent) {
      ProcessUnpublishingCompletedEvent other = (ProcessUnpublishingCompletedEvent) obj;
      return ObjectUtil.safeEquals(process, other.getProcess())
          && ObjectUtil.safeEquals(status, other.getStatus());
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "process", process, "status", status);
  }
}
