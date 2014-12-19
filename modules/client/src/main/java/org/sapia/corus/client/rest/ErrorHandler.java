package org.sapia.corus.client.rest;

import java.io.StringWriter;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.sapia.corus.client.common.json.WriterJsonStream;

/**
 * An instance of this interface generates JSON for any error occuring when invoking a REST method.
 * 
 * @author yduchesne
 *
 */
public interface ErrorHandler {
  
  public static final int SERVER_ERROR = 500;
  
  /**
   * A default {@link ErrorHandler} implementation.
   * 
   * @author yduchesne
   */
  public static class DefaultErrorHandler implements ErrorHandler {
    
    @Override
    public String generateJsonFor(Throwable error, RestResponseFacade response) {
      response.setStatus(SERVER_ERROR);
      response.setContentType(ContentTypes.APPLICATION_JSON);
      StringWriter sw = new StringWriter();
      WriterJsonStream st = new WriterJsonStream(sw);
      st.beginObject()
        .field("message").value(error.getMessage())
        .field("details").value(ExceptionUtils.getFullStackTrace(error))
      .endObject();
      return sw.toString();
    }
  }

  /**
   * @param error the {@link Throwable} instance corresponding to the error that occurred.
   * @param response the {@link RestResponseFacade} hiding the details of the response that will
   * be sent.
   * @return the JSON payload holding the error data.
   */
  public String generateJsonFor(Throwable error, RestResponseFacade response);
}
