package org.sapia.corus.http.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.ResourceNotFoundException;
import org.sapia.corus.client.rest.RestContainer;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.Subject;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.util.Strings;

/**
 * Entry point into the RESTful API.
 * 
 * @author yduchesne
 *
 */
public class RestExtension implements HttpExtension {
    
  private static final int BYTES_PER_CHAR = 4;
  private static HttpExtensionInfo INFO = HttpExtensionInfo.newInstance()
   .setContextPath("/rest")
   .setDescription("Handles REST calls")
   .setName("Corus REST API");

  private Logger         logger     = Hierarchy.getDefaultHierarchy().getLoggerFor(RestExtension.class.getName());
  private CorusConnector connector;
  private RestContainer  container;
  private ServerContext  serverContext;
  
  public RestExtension(ServerContext serverContext) {
    this.serverContext = serverContext;
    connector = new CorusConnectorImpl(
        new RestConnectionContext(
            serverContext.getCorus(), 
            new DefaultClientFileSystem(new File(serverContext.getHomeDir()))
        )
    );
    
    container = RestContainer.Builder.newInstance().buildDefaultInstance();
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
      processRequest(subject, ctx);
    } else {
      try {
        subject = serverContext.getServices().getAppKeyManager().authenticate(appId, appKey);
        processRequest(subject, ctx);
      } catch (CorusSecurityException e) {
        sendErrorResponse(ctx, HttpStatus.SC_FORBIDDEN, e);
      }
    }
  }
  
  private void processRequest(Subject subject, final HttpContext ctx) throws Exception, FileNotFoundException {
    ServerRestRequest request = new ServerRestRequest(ctx);
    String            payload = null;
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
      
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
        logger.debug(payload);
      }
      
      sendOkResponse(ctx, payload);
      
    } catch (FileNotFoundException e) {
      logger.error("Error performing RESTful call (resource not found): " + ctx.getPathInfo());
      sendErrorResponse(ctx, HttpStatus.SC_NOT_FOUND, e);
    } catch (ResourceNotFoundException e) {
      logger.error("Error performing RESTful call (resource not found): " + ctx.getPathInfo());
      sendErrorResponse(ctx, HttpStatus.SC_NOT_FOUND, e);
    } catch (CorusSecurityException e) {
      sendErrorResponse(ctx, HttpStatus.SC_FORBIDDEN, e);
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
      .endObject();
    sendResponse(ctx, status, err.getMessage(), writer.toString());
  }
  
  private void sendOkResponse(HttpContext ctx, String payload) throws IOException {
    if (payload == null) {
      StringWriter writer = new StringWriter();
      JsonStream   stream = new WriterJsonStream(writer);
      stream.beginObject().field("status").value(HttpStatus.SC_OK).endObject();
      sendResponse(ctx, HttpStatus.SC_OK, null, writer.toString());
    } else {
      sendResponse(ctx, HttpStatus.SC_OK, null, payload);
    }
  }
  
  public void sendResponse(HttpContext ctx, int statusCode, String statusMsg, String payload) throws IOException {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(ctx.getResponse().getOutputStream()));
    ctx.getResponse().setContentLength(payload.length() * BYTES_PER_CHAR);
    try {
      ctx.getResponse().setStatusCode(statusCode);
      if (statusMsg != null) {
        ctx.getResponse().setStatusMessage(statusMsg);
      }
      writer.print(payload);
      writer.flush();
    } finally {
      writer.close();
      ctx.getResponse().commit();
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

}
