package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.util.Strings;

/**
 * Dispatched when a process' publishing has completed.
 * 
 * @author yduchesne
 *
 */
public class ProcessPublishingCompletedEvent implements Event {
  
  public enum PublishStatus {
    NOT_APPLICABLE,
    SUCCESS,
    FAILURE,
    MAX_ATTEMPTS_REACHED;
  }
 
  private Process       process;
  private PublishStatus status;
  private OptionalValue<Exception> error = OptionalValue.none();
  
  public ProcessPublishingCompletedEvent(Process process, Exception error) {
    this.process = process;
    this.status  = PublishStatus.FAILURE;
    this.error   = OptionalValue.of(error);
  }
  
  public ProcessPublishingCompletedEvent(Process process, PublishStatus status) {
    this.process = process;
    this.status  = status;
  }
  
  public Process getProcess() {
    return process;
  }

  public PublishStatus getStatus() {
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
    if (obj instanceof ProcessPublishingCompletedEvent) {
      ProcessPublishingCompletedEvent other = (ProcessPublishingCompletedEvent) obj;
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
