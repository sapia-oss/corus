package org.sapia.corus.http;

import java.io.IOException;
import java.io.OutputStream;

import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.simpleframework.http.Response;

/**
 * Implements the {@link HttpResponseFacade} around the {@link Response}
 * interface.
 * 
 * @author yduchesne
 * 
 */
public class DefaultHttpResponseFacade implements HttpResponseFacade {

  private Response response;

  public DefaultHttpResponseFacade(Response response) {
    this.response = response;
  }

  @Override
  public void setHeader(String name, String value) {
    response.setValue(name, value);
  }

  @Override
  public void setStatusCode(int code) {
    response.setCode(code);
  }

  @Override
  public void setContentLength(int len) {
    response.setContentLength(len);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return response.getOutputStream();
  }

  @Override
  public String getHeader(String name) {
    return response.getValue(name);
  }

  @Override
  public void commit() throws IOException {
    response.commit();
  }

}
