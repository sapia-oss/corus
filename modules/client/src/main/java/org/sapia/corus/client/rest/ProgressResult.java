package org.sapia.corus.client.rest;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.http.HttpResponseFacade;

/**
 * Encapsulate a full JSON status response, including server-side feedback if any.
 * 
 * @author yduchesne
 *
 */
public class ProgressResult implements JsonStreamable {
  
  private List<String> messages;
  private Throwable    error;
  private boolean      isError;
  
  public ProgressResult(List<String> messages, Throwable error) {
    this.messages = messages;
    this.error    = error;
  }

  public ProgressResult(List<String> messages) {
    this.messages = messages;
  }
  
  /**
   * Turns on this instance's error flag.
   */
  public ProgressResult error() {
    this.isError = true;
    return this;
  }
  
  /**
   * @return the HTTP status that should be returned in the response headers of the HTTP response.
   */
  public int getStatus() {
    if (error == null && !isError) {
      return HttpResponseFacade.STATUS_OK;
    }
    
    return HttpResponseFacade.STATUS_SERVER_ERROR;
  }
  
  @Override
  public void toJson(JsonStream stream) {
    stream
      .beginObject()
        .field("status").value(getStatus());
    if (error != null) {
      stream.field("stackTrace").value(ExceptionUtils.getStackTrace(error));
    }
    stream.field("feedback").strings(messages);
    stream.endObject();
  }

  // --------------------------------------------------------------------------
  // Visible for testing
 
  Throwable getThrowable() {
    return error;
  }
  
  List<String> getMessages() {
    return messages;
  }
  
  boolean isError() {
    return isError;
  }
  
}
