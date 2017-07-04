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
import org.sapia.corus.client.ClientDebug;
import org.sapia.corus.client.cli.DefaultClientFileSystem;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionServiceImpl;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.ResourceNotFoundException;
import org.sapia.corus.client.rest.RestContainer;
import org.sapia.corus.client.rest.RestContainer.ResourceInvocationResult;
import org.sapia.corus.client.rest.RestResponseFacade;
import org.sapia.corus.client.rest.resources.ProgressResult;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.http.HttpContext;
import org.sapia.corus.client.services.http.HttpExtension;
import org.sapia.corus.client.services.http.HttpExtensionInfo;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.security.CorusSecurityException;
import org.sapia.corus.client.services.security.Subject;
import org.sapia.corus.configurator.PropertyChangeEvent;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.util.LoggerLogCallback;
import org.sapia.ubik.util.Streams;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.pool.Pool;

/**
 * Entry point into the RESTful API.
 * 
 * @author yduchesne
 *
 */
public class RestExtension implements HttpExtension {
    
  private static final int       DEFAULT_CORUS_CONNECTOR_POOL_SIZE = 10;
  private static final TimeValue STALE_ASYNC_TASK_CLEANUP_DELAY    = TimeValue.createSeconds(60);
  private static final TimeValue DEFAULT_TASK_TIMEOUT              = TimeValue.createSeconds(30);
  private static final int       TRANSFER_BUFSZ                    = 8082;
  
  private static HttpExtensionInfo INFO = HttpExtensionInfo.newInstance()
   .setContextPath("/rest")
   .setDescription("Handles REST calls")
   .setName("Corus REST API");
  

  private Logger             logger     = Hierarchy.getDefaultHierarchy().getLoggerFor(RestExtension.class.getName());
  private CorusConnectorPool connectors;
  private RestContainer      container;
  private ServerContext      serverContext;
  private AsynchronousCompletionServiceImpl asyncImpl;
  private PartitionServiceImpl partitionImpl;
  
  public RestExtension(ServerContext serverContext) {
    this.serverContext = serverContext;
    connectors = new CorusConnectorPool(DEFAULT_CORUS_CONNECTOR_POOL_SIZE);
    Auditor auditor = serverContext.getServices().getAuditor();
    container  = RestContainer.Builder.newInstance().auditor(auditor).buildDefaultInstance();
    
    String authRequired = doGetProperty(CorusConsts.PROPERTY_CORUS_REST_AUTH_REQUIRED);
    if (authRequired != null && authRequired.equalsIgnoreCase("true")) {
      container.setAuthRequired(true);
    } else {
      container.setAuthRequired(false);
    }

    asyncImpl = new AsynchronousCompletionServiceImpl(
        new TaskManagerExecutionProvider(serverContext.getServices().getTaskManager()), DEFAULT_TASK_TIMEOUT
    );
    partitionImpl = new PartitionServiceImpl();
    partitionImpl.setLogCallback(new LoggerLogCallback(logger));
    
    serverContext.getServices().getTaskManager().executeBackground(new Task<Void, Void>() {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param)
          throws Throwable {
        asyncImpl.flushStaleTasks();
        partitionImpl.flushStalePartitionSets();
        return null;
      }
    }, null, BackgroundTaskConfig.create().setExecInterval(STALE_ASYNC_TASK_CLEANUP_DELAY.getValueInMillis()));
    serverContext.getServices().getEventDispatcher().addInterceptor(PropertyChangeEvent.class, this);
    
    ClientDebug.setOutput(new ClientDebug.ClientDebugOutput() {
      @Override
      public void println(String line) {
        logger.debug(line);
      }
    });
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
  
  @Override
  public void destroy() {
    asyncImpl.shutdown();
  }
  
  private void processRequest(Subject subject, final HttpContext ctx, CorusConnector connector) throws Exception, FileNotFoundException {
    ServerRestRequest                      request               = new ServerRestRequest(ctx);
    final Reference<Integer>               responseStatusCode    = new DefaultReference<Integer>(HttpStatus.SC_OK);
    OptionalValue<String>                  nullMsg               = OptionalValue.none();
    final Reference<OptionalValue<String>> responseStatusMessage = new DefaultReference<OptionalValue<String>>(nullMsg);
    
    try {
      
      ConnectorPool pool = new ConnectorPool() {
        @Override
        public void release(CorusConnector toRelease) {
          connectors.release(toRelease);
        }
        
        @Override
        public CorusConnector acquire() {
          try {
            return connectors.acquire();
          } catch (Exception e) {
            throw new IllegalStateException("Could not acquire connector from pool", e);
          }
        }
      };
      
      RequestContext           reqCtx           = new RequestContext(subject, request, connector, asyncImpl, partitionImpl, pool);
      ResourceInvocationResult invocationResult = container.invoke(reqCtx, new RestResponseFacade() {
        
        private boolean statusSet, statusMessageSet;
        @Override
        public void setStatus(int statusCode) {
          if (!statusSet) {
            responseStatusCode.set(statusCode);
            statusSet = true;
          }
        }
        
        @Override
        public void setContentType(String contentType) {
          ctx.getResponse().setContentType(contentType);
        }
        
        @Override
        public void setStatusMessage(String msg) {
          if (!statusMessageSet) {
            responseStatusMessage.set(OptionalValue.of(msg));
            statusMessageSet = true;
          }
        }
      });
      
      if (invocationResult.getReturnValue().isNull()) {
        OptionalValue<String> payloadContent = OptionalValue.none();
        sendOkResponse(ctx, responseStatusCode.get(), responseStatusMessage.get(), payloadContent);
      } else if (invocationResult.getReturnValue().get() instanceof ProgressResult) {
        ProgressResult result = (ProgressResult) invocationResult.getReturnValue().get();
        StringWriter   sw     = new StringWriter();
        JsonStream     stream = new WriterJsonStream(sw);
        String         contentLevelName = ctx.getRequest().getParameter("contentLevel");
        ContentLevel   contentLevel = ContentLevel.forNameOrDefault(contentLevelName, invocationResult.getResourceMetadata().getDefaultContentLevel());
        result.toJson(stream, contentLevel);
        String content = sw.toString();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
          logger.debug(content);
        }
        sendOkResponse(ctx, result.getStatus(), responseStatusMessage.get(), OptionalValue.of(content));
      } else if (invocationResult.getReturnValue().get() instanceof String) {
        String szPayload = (String) invocationResult.getReturnValue().get();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
          logger.debug(szPayload);
        }
        sendOkResponse(ctx, responseStatusCode.get(), responseStatusMessage.get(), OptionalValue.of(szPayload));
      } else if (invocationResult.getReturnValue().get() instanceof JsonStreamable) {
        JsonStreamable result = (JsonStreamable) invocationResult.getReturnValue().get();
        StringWriter   sw     = new StringWriter();
        JsonStream     stream = new WriterJsonStream(sw);
        String         contentLevelName = ctx.getRequest().getParameter("contentLevel");
        ContentLevel   contentLevel = ContentLevel.forNameOrDefault(contentLevelName, invocationResult.getResourceMetadata().getDefaultContentLevel());
        result.toJson(stream, contentLevel);
        String content = sw.toString();
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Got response payload for REST call %s:", ctx.getPathInfo()));
          logger.debug(content);
        }
        sendOkResponse(ctx, HttpResponseFacade.STATUS_OK, responseStatusMessage.get(), OptionalValue.of(content));
      } else if (invocationResult.getReturnValue().get() instanceof InputStream) {
        sendReponse(ctx, (InputStream) invocationResult.getReturnValue().get()); 
      } else {
        throw new IllegalStateException("Illegal payload type: " + invocationResult.getReturnValue().get().getClass().getName());
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
    OptionalValue<String> errMsg = OptionalValue.of(err.getMessage());
    sendResponse(ctx, status, errMsg,  writer.toString());
  }
  
  private void sendOkResponse(HttpContext ctx, int statusCode, OptionalValue<String> statusMsg, OptionalValue<String> payload) throws IOException {
    if (payload.isNull()) {
      StringWriter writer = new StringWriter();
      JsonStream   stream = new WriterJsonStream(writer);
      stream.beginObject().field("status").value(HttpStatus.SC_OK).field("feedback").strings(new String[]{}).endObject();
      sendResponse(ctx, statusCode, statusMsg, writer.toString());
    } else {
      sendResponse(ctx, statusCode, statusMsg, payload.get());
    }
  }
  
  private void sendResponse(HttpContext ctx, int statusCode, OptionalValue<String> statusMsg, String payload) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(ctx.getResponse().getOutputStream());
    byte[] content = payload.getBytes("UTF-8");
    try {
      
      ctx.getResponse().setHeader("Access-Control-Allow-Origin", "*");
      ctx.getResponse().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      ctx.getResponse().setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      ctx.getResponse().setHeader("Allow", "GET, POST, PUT, DELETE, OPTIONS");
      
      ctx.getResponse().setStatusCode(statusCode);
      if (statusMsg.isSet()) {
        ctx.getResponse().setStatusMessage(statusMsg.get());
      }
     
      if (logger.isDebugEnabled()) {
        logger.debug("Sending response:");
        logger.debug(payload);
      }
      
      bos.write(content);
      bos.flush();
    } finally {
      bos.close();
      ctx.getResponse().commit();
    }
  }
  
  private void sendReponse(HttpContext ctx, InputStream is) throws IOException {
    ctx.getResponse().setHeader("Access-Control-Allow-Origin", "*");
    ctx.getResponse().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    ctx.getResponse().setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    ctx.getResponse().setHeader("Allow", "GET, POST, PUT, DELETE, OPTIONS");
    ctx.getResponse().setStatusCode(HttpStatus.SC_OK);
    BufferedOutputStream bos = new BufferedOutputStream(ctx.getResponse().getOutputStream(), TRANSFER_BUFSZ);
    byte[] buf = new byte[TRANSFER_BUFSZ];
    int read;
    try {
      while ((read = is.read(buf)) > -1) {
        bos.write(buf, 0, read);
        bos.flush();
      }
    } finally {
      Streams.closeSilently(bos);
      Streams.closeSilently(is);
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
    public String getRemoteHost() {
      return delegate.getRequest().getRemoteHost();
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
