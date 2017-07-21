package org.sapia.corus.client.services.processor.event;

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
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.ubik.util.Strings;

/**
 * Dispatched when a process' publishing has completed.
 * 
 * @author yduchesne
 *
 */
public class ProcessPublishingCompletedEvent extends CorusEventSupport {
  
  public enum PublishStatus {
    NOT_APPLICABLE,
    SUCCESS,
    FAILURE,
    MAX_ATTEMPTS_REACHED;
  }
 
  private Process                  process;
  private PublishStatus            status;
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
  // CorusEventSupport impl.
  
  @Override
  public EventLevel getLevel() {
    switch (status) {
      case FAILURE:
        return EventLevel.ERROR;
      case MAX_ATTEMPTS_REACHED:
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
          msg.append(String.format("Error occurred while publishing process %s: %s. Check the log files for more details", 
              ToStringUtil.toString(process), err.getMessage()));
        })
        .ifNull(() -> {
          msg.append(String.format("Error occurred while publishing process %s. Check the log files for more details",
              ToStringUtil.toString(process)));
        });
        break;
      case MAX_ATTEMPTS_REACHED:
        msg.append(String.format("Max publishing attempts reached while trying to publish process %s", 
            ToStringUtil.toString(process)));
        break;
      case NOT_APPLICABLE:
        msg.append(String.format("Publishing does not apply to process %s", 
            ToStringUtil.toString(process)));
        break;
      case SUCCESS:
        msg.append(String.format("Publishing completred successfully for process %s", 
            ToStringUtil.toString(process)));
        break;
      default:
        throw new IllegalStateException("Status not handled: " + status);
    }
    
    return EventLog.builder()
        .source(Processor.class)
        .type(getClass())
        .level(getLevel())
        .message("message", msg)
        .build();
  }
  
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
      stream.field("error")
        .value(StringEscapeUtils.escapeJavaScript(ExceptionUtils.getFullStackTrace(err)));
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
