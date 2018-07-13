package org.sapia.corus.client.services.processor.event;

import javax.annotation.processing.Processor;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Strings;

/**
 * Dispatched when a process' unpublishing has completed.
 * 
 * @author yduchesne
 *
 */
public class ProcessUnpublishingCompletedEvent extends CorusEventSupport {
  
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
  
  @Override
  public EventLevel getLevel() {
    switch (status) {
      case FAILURE:
        return EventLevel.ERROR;
      case NOT_APPLICABLE:
      case SUCCESS:
        return EventLevel.INFO;
      default:
        throw new IllegalStateException("Status not handled: " + status);
    }
  }
  
  @Override
  public EventLog toEventLog() {
    final StringBuilder msg = new StringBuilder();
    switch (status) {
      case FAILURE:
        error.ifSet(err -> {
          msg.append(String.format("Error occurred while unpublishing process %s: %s. Check the log files for more details", 
              ToStringUtil.toString(process), err.getMessage()));
        })
        .ifNull(() -> {
          msg.append(String.format("Error occurred while unpublishing process %s. Check the log files for more details",
              ToStringUtil.toString(process)));
        });
        break;
      case NOT_APPLICABLE:
        msg.append(String.format("Unpublishing does not apply to process %s", 
            ToStringUtil.toString(process)));
        break;
      case SUCCESS:
        msg.append(String.format("Unpublishing completed successfully for process %s", 
            ToStringUtil.toString(process)));
        break;
      default:
        throw new IllegalStateException("Status not handled: " + status);
    }
  
    return EventLog.builder()
        .source(source())
        .type(getClass())
        .level(getLevel())
        .message("message", msg)
        .build();
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected Class<?> source() {
    return Processor.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("status").value(status.name())
      .field("message").value(toEventLog().getMessage())
      .field("process");
    process.toJson(stream, ContentLevel.SUMMARY);
    
    error.ifSet(err -> {
      stream.field("error").value(StringEscapeUtils.escapeJavaScript(ExceptionUtils.getFullStackTrace(err)));
    });
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
