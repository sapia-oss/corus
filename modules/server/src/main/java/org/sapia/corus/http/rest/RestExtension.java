package org.sapia.corus.http.rest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.cli.DefaultClientFileSystem;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.corus.client.rest.ProgressResult;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.ResourceNotFoundException;
import org.sapia.corus.client.rest.RestContainer;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.Subject;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.pool.Pool;

/**
 * Entry point into the RESTful API.
 * 
 * @author yduchesne
 *
 */
public class RestExtension implements HttpExtension, Interceptor {
    
  private static final int DEFAULT_CORUS_CONNECTOR_POOL_SIZE = 5;
  
  private static HttpExtensionInfo INFO = HttpExtensionInfo.newInstance()
   .setContextPath("/rest")
   .setDescription("Handles REST calls")
   .setName("Corus REST API");

  private Logger             logger     = Hierarchy.getDefaultHierarchy().getLoggerFor(RestExtension.class.getName());
  private CorusConnectorPool connectors;
  private RestContainer      container;
  private ServerContext      serverContext;
  
  public RestExtension(ServerContext serverContext) {
    this.serverContext = serverContext;
    connectors = new CorusConnectorPool(DEFAULT_CORUS_CONNECTOR_POOL_SIZE);
    container  = RestContainer.Builder.newInstance().buildDefaultInstance();
    
    String authRequired = doGetProperty(CorusConsts.PROPERTY_CORUS_REST_AUTH_REQUIRED);
    if (authRequired != null && authRequired.equalsIgnoreCase("true")) {
      container.setAuthRequired(true);
    } else {
      container.setAuthRequired(false);
    }

    serverContext.getServices().getEventDispatcher().addInterceptor(PropertyChangeEvent.class, this);
  }
  
  private String doGetProperty(String propName) {
    String value = serverContext
        .getCorusProperties()
        .getProperty(propName);
    
    String overridde = serverContext.getServices().getConfigurator()
        .getProperties(PropertyScope.SERVER, new ArrayList<String>(0))
        .getProperty(propName);
    if (overridde != null) {
      value = overridde;
    }
    return value;
  }
  
  @Override
  public HttpExtensionInfo getInfo() {
    return INFO;
  }
  
  @Override
  public void process(final HttpContext ctx) throws Exception, FileNotFoundException {
    
    if (logger.isDebugEnabled()) {
      logger.debug(String.format("Receiving REST call %s", ctx.getPathInfo()));
      logger.debug(String.format("=> HTTP method...........: %s", ctx.getRequest().getMethod()));
      logger.debug(String.format("=> Accept header values..: %s", ctx.getRequest().getAccepts()));
      logger.debug(String.format("=> Content type..........: %s", ctx.getRequest().getContentType()));
    }
    
    String appKey = ctx.getRequest().getHeader(CORUS_HEADER_APP_KEY);
    if (Strings.isBlank(appKey)) {
      appKey = ctx.getRequest().getParameter(CORUS_PARAM_APP_KEY);
    }
    String appId  = ctx.getRequest().getHeader(CORUS_HEADER_APP_ID);
    if (Strings.isBlank(appId)) {
      appId  = ctx.getRequest().getParameter(CORUS_PARAM_APP_ID);
    }
    
    Subject subject;
    if (Strings.isBlank(appKey) || Strings.isBlank(appId)) {
      logger.debug("Application key or application ID not specified: giving anonymous access");
      subject = Subject.Anonymous.newInstance();
      CorusConnector connector = connectors.acquire();
      try {
        processRequest(subject, ctx, connector);
      } finally {
        connectors.release(connector);
      }
    } else {
      CorusConnector connector = connectors.acquire();
      try {
        subject = serverContext.getServices().getAppKeyManager().authenticate(appId, appKey);
        processRequest(subject, ctx, connector);
      } catch (CorusSecurityException e) {
        sendErrorResponse(ctx, HttpStatus.SC_FORBIDDEN, e);
      } finally {
        connectors.release(connector);
      }
    }
  }
  
  private void processRequest(Subject subject, final HttpContext ctx, CorusConnector connector) throws Exception, FileNotFoundException {
    ServerRestRequest request = new ServerRestRequest(ctx);
    Object            payload = null;
    try {
      payload = container.invoke(new RequestContext(subject, request, connector), new RestResponseFacade() {
        @Override
        public void setStatus(int statusCode) {
          ctx.getResponse().setStatusCode(statusCode);
        }
        
        @Override
        public void setContentType(String contentType) {
          ctx.getResponse().setContentType(contentType);
        }
        
        @Override
        public void setStatusMessage(String msg) {
          ctx.getResponse().setStatusMessage(msg);
        }
      });
      
      if (payload == null) {
        sendOkResponse(ctx, null);
      } else if (payload instanceof ProgressResult) {
        ProgressResult result = (ProgressResult) payload;
        StringWriter   sw     = new StringWriter();
        JsonStream     stream = new WriterJsonStream(sw);
        result.toJson(stream);
        String content = sw.toString();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
          logger.debug(content);
        }
        sendResponse(ctx, result.getStatus(), null, content);
        
      } else if (payload instanceof String) {
        String szPaylaod = (String) payload;
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
          logger.debug(szPaylaod);
        }
        sendOkResponse(ctx, szPaylaod);
      } else {
        throw new IllegalStateException("Unhandled payload type: " + payload);
      }
      
    } catch (FileNotFoundException e) {
      logger.error("Error performing RESTful call (resource not found): " + ctx.getPathInfo());
      sendErrorResponse(ctx, HttpStatus.SC_NOT_FOUND, e);
    } catch (ResourceNotFoundException e) {
      logger.error("Error performing RESTful call (resource not found): " + ctx.getPathInfo());
      sendErrorResponse(ctx, HttpStatus.SC_NOT_FOUND, e);
    } catch (CorusSecurityException e) {
      sendErrorResponse(ctx, HttpStatus.SC_FORBIDDEN, e);
    } catch (IllegalArgumentException e) {
      logger.error("Error performing RESTful call: " + ctx.getPathInfo(), e);
      sendErrorResponse(ctx, HttpStatus.SC_NOT_ACCEPTABLE, e);
    } catch (Throwable e) {
      logger.error("Error performing RESTful call: " + ctx.getPathInfo(), e);
      sendErrorResponse(ctx, HttpStatus.SC_INTERNAL_SERVER_ERROR, e);
    }    
  }

  private void sendErrorResponse(HttpContext ctx, int status, Throwable err) throws IOException {
    StringWriter writer = new StringWriter();
    JsonStream   stream = new WriterJsonStream(writer);
    stream
      .beginObject()
        .field("status").value(status)
        .field("stackTrace").value(ExceptionUtils.getStackTrace(err))
        .field("feedback").strings(new String[]{})
      .endObject();
    sendResponse(ctx, status, err.getMessage(), writer.toString());
  }
  
  private void sendOkResponse(HttpContext ctx, String payload) throws IOException {
    if (payload == null) {
      StringWriter writer = new StringWriter();
      JsonStream   stream = new WriterJsonStream(writer);
      stream.beginObject().field("status").value(HttpStatus.SC_OK).field("feedback").strings(new String[]{}).endObject();
      sendResponse(ctx, HttpStatus.SC_OK, null, writer.toString());
    } else {
      sendResponse(ctx, HttpStatus.SC_OK, null, payload);
    }
  }
  
  public void sendResponse(HttpContext ctx, int statusCode, String statusMsg, String payload) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(ctx.getResponse().getOutputStream());
    byte[] content = payload.getBytes("UTF-8");
    try {
      
      ctx.getResponse().setHeader("Access-Control-Allow-Origin", "*");
      ctx.getResponse().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      ctx.getResponse().setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      ctx.getResponse().setHeader("Allow", "GET, POST, PUT, DELETE, OPTIONS");
      
      ctx.getResponse().setStatusCode(statusCode);
      if (statusMsg != null) {
        ctx.getResponse().setStatusMessage(statusMsg);
      }
      bos.write(content);
      bos.flush();
    } finally {
      bos.close();
      ctx.getResponse().commit();
    }
  }
  
  /**
   * @param event
   *          a {@link PropertyChangeEvent}.
   */
  public void onPropertyChangeEvent(PropertyChangeEvent event) {
    if (event.getScope() == PropertyScope.SERVER) {
      if (event.containsProperty(CorusConsts.PROPERTY_CORUS_REST_AUTH_REQUIRED)) {
        if (EventType.ADD == event.getEventType()) {
          String authRequiredValue = doGetProperty(CorusConsts.PROPERTY_CORUS_REST_AUTH_REQUIRED);
          container.setAuthRequired(Boolean.parseBoolean(authRequiredValue));
        } else if (EventType.REMOVE == event.getEventType()) {
          String authRequiredValue = serverContext.getCorusProperties().getProperty(CorusConsts.PROPERTY_CORUS_REST_AUTH_REQUIRED);
          container.setAuthRequired(Boolean.parseBoolean(authRequiredValue));
        }
      }
    }
  }

  
  // ==========================================================================
  
  public class ServerRestRequest implements RestRequest {
    
    private HttpContext delegate;
    
    ServerRestRequest(HttpContext delegate) {
      this.delegate = delegate;
    }
    
    @Override
    public String getContentType() {
      return delegate.getRequest().getContentType();
    }
    
    @Override
    public Set<String> getAccepts() {
      return delegate.getRequest().getAccepts();
    }
    
    @Override
    public String getMethod() {
      return delegate.getRequest().getMethod();
    }
    
    @Override
    public String getPath() {
      return delegate.getPathInfo();
    }
    
    @Override
    public Value getValue(String name) {
      String value = delegate.getRequest().getParameter(name);
      if (value == null) {
        value = delegate.getRequest().getHeader(name);
      }
      return new Value(name, value);
    }
    
    @Override
    public Value getValue(String name, String defaultVal) {
      String value = delegate.getRequest().getParameter(name);
      if (value == null) {
        value = delegate.getRequest().getHeader(name);
      }
      if (value == null) {
        value = defaultVal;
      }
      return new Value(name, value);
    }
    
    @Override
    public List<Value> getValues() {
      List<Value> toReturn = new ArrayList<>();
      for (Map.Entry<String, String> entry : delegate.getRequest().getParameters().entrySet()) {
        toReturn.add(new Value(entry.getKey(), entry.getValue()));
      } 
      return toReturn;
    }
    
    @Override
    public InputStream getContent() throws IOException {
      return delegate.getRequest().getInputStream();
    }
     
    @Override
    public long getContentLength() {
      String value = delegate.getRequest().getHeader("Content-Length");
      if (value == null) {
        return 0;
      }
      return Long.parseLong(value);
    }
  }
  
  class CorusConnectorPool extends Pool<CorusConnector> {
    
    public CorusConnectorPool(int maxSize) {
      super(maxSize);
    }
    
    @Override
    protected CorusConnector doNewObject() throws Exception {
      return new CorusConnectorImpl(
          new RestConnectionContext(
              serverContext.getCorus(), 
              new DefaultClientFileSystem(
                  new File(serverContext.getServices().getDeployer().getConfiguration().getUploadDir())
              )
          )
      );
    }
    
  }

}
