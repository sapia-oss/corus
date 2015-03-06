package org.sapia.corus.client.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.services.security.Subject;
import org.sapia.corus.client.services.security.Subject.Anonymous;

/**
 * Holds data corresponding to in incoming REST request.
 * 
 * @author yduchesne
 *
 */
public class RequestContext {

  private Subject        subject;
  private RestRequest    request;
  private CorusConnector connector;
  
  public RequestContext(Subject subject, RestRequest request, CorusConnector connector) {
    this.subject   = subject;
    this.request   = request;
    this.connector = connector;
  }
  
  public RequestContext(RestRequest request, CorusConnector connector) {
    this(Anonymous.newInstance(), request, connector);
  }
  
  /**
   * @return this instance's {@link Subject}.
   */
  public Subject getSubject() {
    return subject;
  }
  
  /**
   * @return this instance's {@link RestRequest}.
   */
  public RestRequest getRequest() {
    return this.request;
  }
  
  /**
   * @param params the REST request's additional parameters (typically parsed out of the rest resource path).
   */
  public void addParams(Map<String, String> params) {
    ParamRequest paramReq;
    if (request instanceof ParamRequest) {
      paramReq = (ParamRequest) request;
    } else {
      paramReq = new ParamRequest(request);
      request = paramReq;
    }
    paramReq.params.putAll(params);
  }
  
  /**
   * @return this instance's {@link CorusConnector}.
   */
  public CorusConnector getConnector() {
    return connector;
  }
  
  // ==========================================================================
  // Inner class
  
  static class ParamRequest implements RestRequest {
    
    private RestRequest         delegate;
    private Map<String, String> params = new HashMap<String, String>();
    
    private ParamRequest(RestRequest delegate) {
      this.delegate = delegate;
    }
    
    @Override
    public Set<String> getAccepts() {
      return delegate.getAccepts();
    }
    
    @Override
    public String getContentType() {
      return delegate.getContentType();
    }
    
    @Override
    public String getMethod() {
      return delegate.getMethod();
    }
    
    @Override
    public String getPath() {
      return delegate.getPath();
    }
    
    @Override
    public Value getValue(String name) {
      String value = params.get(name);
      if (value != null) {
        return new Value(name, value);
      }
      return delegate.getValue(name);
    }   
    
    @Override
    public Value getValue(String name, String defaultVal) {
      String value = params.get(name);
      if (value != null) {
        return new Value(name, value);
      }
      return delegate.getValue(name, defaultVal); 
    }
    
    @Override
    public List<Value> getValues() {
      return delegate.getValues();
    }
    
    @Override
    public InputStream getContent() throws IOException {
      return delegate.getContent();
    }
    
    @Override
    public long getContentLength() {
      return delegate.getContentLength();
    }
    
  }
}
