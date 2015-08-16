package org.sapia.corus.client.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.http.HttpResponseFacade;

/**
 * Encapsulate a full JSON status response, including server-side feedback if any.
 * 
 * @author yduchesne
 *
 */
public class ProgressResult implements JsonStreamable {
  
  private static final int BASE_HTTP_ERROR_CODE = 400;
  
  private OptionalValue<String>         completionToken = OptionalValue.none();
  private List<JsonStreamable>          nestedJson      = new ArrayList<JsonStreamable>();
  private List<String>                  messages        = new ArrayList<String>();
  private OptionalValue<Throwable>      error           = OptionalValue.none();
  private boolean                       isError;
  private int                           status;
  private Set<CorusHost>                processedHosts  = new HashSet<CorusHost>();
  
  public ProgressResult() {
  }
  
  public ProgressResult(List<String> messages, Throwable error) {
    this.messages.addAll(messages);
    this.error    = OptionalValue.of(error);
    this.isError  = true;
  }
  
  public ProgressResult(List<String> messages) {
    this.messages.addAll(messages);
  }
  
  public ProgressResult setCompletionToken(String completionToken) {
    this.completionToken = OptionalValue.of(completionToken);
    return this;
  }
  
  public ProgressResult withNestedJson(JsonStreamable nested) {
    this.nestedJson.add(nested);
    return this;
  }
  
  public ProgressResult addMessage(String msg) {
    this.messages.add(msg);
    return this;
  }
  
  /**
   * Turns on this instance's error flag.
   */
  public ProgressResult error() {
    this.isError = true;
    return this;
  }

  /**
   * @return <code>true</code> if the operation to which this instance corresponds resulted in an error.
   */
  public boolean isError() {
    return isError || getStatus() >= BASE_HTTP_ERROR_CODE;
  }
  
  /**
   * @return the HTTP status that should be returned in the response headers of the HTTP response.
   */
  public int getStatus() {
    if (error.isNull() && !isError) {
      if (status != 0) {
        return status;
      } else {
        return HttpResponseFacade.STATUS_OK;
      }
    }
    
    return HttpResponseFacade.STATUS_SERVER_ERROR;
  }
  
  /**
   * @param status the status code to assign to this instance.
   */
  public ProgressResult setStatus(int status) {
    this.status = status;
    return this;
  }
  
  /**
   * @param result a {@link ProgressResult} to merge with this instance.
   */
  public void merge(ProgressResult result) {
    if (!isError()) {
      messages.addAll(result.messages);
      nestedJson.addAll(result.nestedJson);
      processedHosts.addAll(result.processedHosts);
      
      error   = result.error;
      isError = result.isError;
      if (result.status != 0) {
        this.status = result.status;
      }
    }
  }
  
  /**
   * @param results {@link Collection} of {@link ProgressResult}s to merge with 
   * this instance.
   */
  public void merge(Collection<ProgressResult> results) {
    for (ProgressResult r : results) {
      merge(r);
    }
  }

  /**
   * @return the {@link Set} of {@link CorusHost}s for which processing was successfully completed.
   */
  public Set<CorusHost> getProcessedHosts() {
    return processedHosts;
  }
  
  public ProgressResult addProcessedHosts(Collection<CorusHost> batch) {
    this.processedHosts.addAll(batch);
    return this;
  }
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream
      .beginObject()
        .field("status").value(getStatus());
    if (error.isSet()) {
      stream.field("stackTrace").value(ExceptionUtils.getStackTrace(error.get()));
    }
    
    if (completionToken.isSet()) {
      stream.field("completionToken").value(completionToken.get());
    }
    
    stream.field("feedback").strings(messages);
    
    if (!processedHosts.isEmpty()) {
      
      if (!processedHosts.isEmpty()) {
        stream.field("processsedHosts").beginArray();
        for (CorusHost h : processedHosts) {
          h.toJson(stream, level);
        }
        stream.endArray();
      }
    }
    
    for (JsonStreamable n : nestedJson) {
      n.toJson(stream, level);
    }
    
    stream.endObject();
  }

  // --------------------------------------------------------------------------
  // Visible for testing
 
  Throwable getThrowable() {
    return error.isSet() ? error.get() : null;
  }
  
  List<String> getMessages() {
    return messages;
  }
  
}
