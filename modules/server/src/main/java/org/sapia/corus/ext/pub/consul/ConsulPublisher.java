package org.sapia.corus.ext.pub.consul;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.deployer.dist.ConsulPublisherConfig;
import org.sapia.corus.client.services.deployer.dist.HttpDiagnosticConfig;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.publisher.ProcessPublishingProvider;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.sapia.ubik.util.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Publishes a service description corresponding to this Corus node to the configured Consul agent.
 * 
 * @author yduchesne
 *
 */
public class ConsulPublisher extends ModuleHelper implements ProcessPublishingProvider {
  
  public static final String ROLE = ProcessPublishingProvider.class.getName() + ".CONSUL";
  
  private static final int HTTP_CONNECT_TIMEOUT = 10000;

  @Autowired
  private InternalConfigurator configurator;
  
  @Autowired
  private TaskManager tasks;
  
  private DynamicProperty<Boolean> publishingEnabled      = new DynamicProperty<Boolean>(false);
  private DynamicProperty<String>  agentUrl               = new DynamicProperty<String>();
  private DynamicProperty<Integer> publishIntervalSeconds = new DynamicProperty<Integer>();
  private DynamicProperty<Integer> publishTtlSeconds      = new DynamicProperty<Integer>();
  
  private volatile Task<Void, Void> publishTask;
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }
  
  void setTasks(TaskManager tasks) {
    this.tasks = tasks;
  }
  
  boolean isPublishTaskRunning() {
    return publishTask != null;
  }
  
  // --------------------------------------------------------------------------
  // Configuration setters
  
  public void setEnabled(boolean enabled) {
    publishingEnabled.setValue(enabled);
  }
  
  public void setAgentUrl(String url) {
    agentUrl.setValue(url);
  }
  
  public void setPublishIntervalSeconds(int publishIntervalSeconds) {
    this.publishIntervalSeconds.setValue(publishIntervalSeconds);
  }
  
  public void setPublishTtlSeconds(int publishTtlSeconds) {
    this.publishTtlSeconds.setValue(publishTtlSeconds);
  }
  
  public void setPublishingEnabled(boolean publishingEnabled) {
    this.publishingEnabled.setValue(publishingEnabled);;
  }
  
  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_EXT_PUB_CONSUL_ENABLED, publishingEnabled);
  
    // taking into account config update
    publishingEnabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        if (property.getValue()) {
          logger().info("Publishing to Consul enabled");
          startPublishCorusHostTask();
        } else {
          logger().info("Publishing to Consul disabled");
        }
      }
    });
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  @Override
  public void start() throws Exception {
    startPublishCorusHostTask();
  }
  
  // --------------------------------------------------------------------------
  // PublishingProvider interface
  
  @Override
  public boolean accepts(ProcessPubConfig config) {
    return this.publishingEnabled.getValueNotNull() && config instanceof ConsulPublisherConfig;
  }
  
  @Override
  public void publish(ProcessPubContext context, PublishingCallback callback) {
    try {
      callback.publishingStarted(context);
      doPublish(context);
      callback.publishingSuccessful(context);
    } catch (Exception e) {
      callback.publishingFailed(context, e);
    }
  }
 
  private void doPublish(ProcessPubContext context) {
    ConsulPublisherConfig pubConf     = (ConsulPublisherConfig) context.getPubConfig();
    String                serviceName = 
        pubConf.getServiceName().isNull() 
        ? context.getProcess().getDistributionInfo().getName() + "-" + context.getProcess().getDistributionInfo().getProcessName()
        : pubConf.getServiceName().get();
        
    Port portConf = context.getProcessConfig().getPortByName(context.getPort().getName()).get();
    
    Assertions.illegalState(
        portConf.getDiagnosticConfig().isNull(), 
        "Diagnostic config not set for port: %s of process %s",
        portConf.getName(), ToStringUtil.toString(context.getProcess())
    );
    
    Assertions.illegalState(
        !(portConf.getDiagnosticConfig().get() instanceof HttpDiagnosticConfig), 
        "Expected HttpDiagnostic config for port: %s of process %s. Got: %s",
        portConf.getName(), ToStringUtil.toString(context.getProcess()),
        portConf.getDiagnosticConfig().get()
    );
        
    HttpDiagnosticConfig diagnosticConf = (HttpDiagnosticConfig) portConf.getDiagnosticConfig().get();
    int servicePort = diagnosticConf.getPortPrefix() > 0 
        ? Integer.parseInt("" + diagnosticConf.getPortPrefix() + context.getPort().getPort()) 
        : context.getPort().getPort();
        
    Set<String> tags = new HashSet<String>();
    tags.add(serverContext().getDomain());
    tags.addAll(context.getDistribution().getTagSet());
    tags.addAll(context.getProcessConfig().getTagSet());

    StringWriter sw     = new StringWriter();
    JsonStream   stream = new WriterJsonStream(sw);
    Endpoint     ep     = serverContext().getCorusHost().getEndpoint();
    
    List<String> categories = new ArrayList<String>();
    categories.addAll(context.getDistribution().getPropertyCategories());
    categories.addAll(context.getProcessConfig().getPropertyCategories());
    PropertiesStrLookup processProps = new PropertiesStrLookup(configurator.getProperties(PropertyScope.PROCESS, categories));
    StrSubstitutor subs = new StrSubstitutor(processProps);
    stream
      .beginObject()
        .field("id").value(serviceName + "-" + servicePort)
        .field("name").value(serviceName)
        .field("tags").strings(new ArrayList<String>(tags))
        .field("address").value(ep.getServerTcpAddress().getHost())
        .field("port").value(servicePort)
        .field("checks").beginArray()
          .beginObject()
            .field("id").value("check-" + serviceName + "-" + servicePort)
            .field("name").value("check-" + serviceName)
            .field("http").value(diagnosticConf.getProtocol() + "://" 
                + serverContext().getCorusHost().getEndpoint().getServerTcpAddress().getHost() 
                + ":" + servicePort + diagnosticConf.getPath())
            .field("interval").value(subs.replace(pubConf.getCheckInterval()) + "s")
            .field("timeout").value(subs.replace(pubConf.getCheckTimeout()) + "s")
          .endObject()
        .endArray()
      .endObject();
    
    if (logger().isDebugEnabled()) {
      logger().debug("Sending process publishing payload to Consul: " + sw.toString());
    }
    
    try {
      URL url = new URL(
          agentUrl.getValue() + (agentUrl.getValue().endsWith("/") ? "" : "/") + "v1/agent/service/register"
      );
      ConsulResponseFacade resp = doSendPayload(url, sw.toString(), "PUT");
      
      if(resp.getStatusCode() != HttpResponseFacade.STATUS_OK) {
        throw new IllegalStateException("Could not publish - got error from Consul agent: " + resp.getStatusCode() + " - " + resp.getStatusMessage());
      } 
      resp.disconnect();
    } catch (IOException e) {
      throw new IllegalStateException("Could not connect to Consul agent at: " + agentUrl, e);
    }
  }
  
  @Override
  public void unpublish(ProcessPubContext context, UnpublishingCallback callback) {
    try {
      callback.unpublishingStarted(context);
      doUnpublish(context);
      callback.unpublishingSuccessful(context);
    } catch (Exception e) {
      callback.unpublishingFailed(context, e);
    }
  }
  
  private void doUnpublish(ProcessPubContext context) {
    ConsulPublisherConfig pubConf     = (ConsulPublisherConfig) context.getPubConfig();
    String                serviceName = 
        pubConf.getServiceName().isNull() 
        ? context.getProcess().getDistributionInfo().getName() + "-" + context.getProcess().getDistributionInfo().getProcessName()
        : pubConf.getServiceName().get();
        
    Port portConf = context.getProcessConfig().getPortByName(context.getPort().getName()).get();
    
    Assertions.illegalState(
        portConf.getDiagnosticConfig().isNull(), 
        "Diagnostic config not set for port: %s of process %s",
        portConf.getDiagnosticConfig(), ToStringUtil.toString(context.getProcess())
    );
    
    Assertions.illegalState(
        !(portConf.getDiagnosticConfig().get() instanceof HttpDiagnosticConfig), 
        "Expected HttpDiagnostic config for port: %s of process %s. Got: %s",
        portConf.getDiagnosticConfig(), ToStringUtil.toString(context.getProcess()),
        portConf.getDiagnosticConfig().get()
    );
        
    HttpDiagnosticConfig diagnosticConf = (HttpDiagnosticConfig) portConf.getDiagnosticConfig().get();
    int servicePort = diagnosticConf.getPortPrefix() > 0 
        ? Integer.parseInt("" + diagnosticConf.getPortPrefix() + context.getPort().getPort()) 
        : context.getPort().getPort();
    
    try {
      URL url = new URL(
          agentUrl.getValue() 
          + (agentUrl.getValue().endsWith("/") ? "" : "/") 
          + "v1/agent/service/unregister/" 
          + serviceName + "-" + servicePort
      );
      ConsulResponseFacade resp = doInvokeUrl(url, "DELETE");
      
      if(resp.getStatusCode() != HttpResponseFacade.STATUS_OK) {
        throw new IllegalStateException("Could not publish - got error from Consul agent: " + resp.getStatusCode() + " - " + resp.getStatusMessage());
      } 
      resp.disconnect();
    } catch (IOException e) {
      throw new IllegalStateException("Could not connect to Consul agent at: " + agentUrl, e);
    }    
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private synchronized void startPublishCorusHostTask() {
    if (publishTask == null && publishingEnabled.getValueNotNull()) {
      publishTask = new Task<Void, Void>("ConsulPublishTask") {
        public Void execute(org.sapia.corus.taskmanager.core.TaskExecutionContext ctx, Void param) throws Throwable 
        {
          if (publishingEnabled.getValueNotNull()) {
            doPublishCorusHost();
          } else {
            abort();
            publishTask = null;
          }
          return null;
        }
      };
      
      tasks.executeBackground(
        publishTask, 
        null, 
        new BackgroundTaskConfig().setExecInterval(
            TimeUnit.MILLISECONDS.convert(publishIntervalSeconds.getValueNotNull(), TimeUnit.SECONDS)
        )
      );
    }
  }
  
  private void doPublishCorusHost() {
    StringWriter sw = new StringWriter();
    JsonStream stream = new WriterJsonStream(sw);
    Endpoint ep = serverContext().getCorusHost().getEndpoint();
    stream
      .beginObject()
        .field("id").value("corus-" + serverContext().getCorusHost().getEndpoint().getServerTcpAddress().getPort())
        .field("name").value("corus")
        .field("tags").strings(new String[] {
            serverContext().getCorus().getDomain()
         })
        .field("address").value(ep.getServerTcpAddress().getHost())
        .field("port").value(ep.getServerTcpAddress().getPort())
        .field("checks").beginArray()
          .beginObject()
            .field("id").value("check-corus-" + serverContext().getCorusHost().getFormattedAddress())
            .field("name").value("Health check for Corus node at: " + serverContext().getCorusHost().getFormattedAddress())
            .field("ttl").value(publishTtlSeconds.getValueNotNull() + "s")
          .endObject()
        .endArray()
      .endObject();
    
    if (logger().isDebugEnabled()) {
      logger().debug("Sending publish payload to Consul: " + sw.toString());
    }
    
    try {
      URL url = new URL(
          agentUrl.getValue() + (agentUrl.getValue().endsWith("/") ? "" : "/") + "v1/agent/service/register"
      );
      ConsulResponseFacade resp = doSendPayload(url, sw.toString(), "PUT");
      
      if(resp.getStatusCode() != HttpResponseFacade.STATUS_OK) {
        logger().error("Could not publish - got error from Consul agent: " + resp.getStatusCode() + " - " + resp.getStatusMessage());
      } 
      resp.disconnect();
    } catch (IOException e) {
      logger().error("Could not connect to Consul agent at: " + agentUrl, e);
    }
  }
  
  
  // --------------------------------------------------------------------------
  // Visible for testing (overridde to bypass real http connection)
  
  ConsulResponseFacade doSendPayload(URL url, String payloadContent, String httpMethod) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod(httpMethod);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
    conn.setReadTimeout(HTTP_CONNECT_TIMEOUT);

    byte[] payload = payloadContent.getBytes();
    conn.setRequestProperty("Content-Length", "" + payload.length);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(payload);
      os.flush();
    }
    
    final String statusMsg  = conn.getResponseMessage();
    final int    statusCode = conn.getResponseCode();
    
    return new ConsulResponseFacade() {
      @Override
      public String getStatusMessage() {
        return statusMsg;
      }
      
      @Override
      public int getStatusCode() {
        return statusCode;
      }
      
      @Override
      public void disconnect() {
        conn.disconnect();
      }
    };
  }
  
  ConsulResponseFacade doSend(URL url, String httpMethod) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(false);
    conn.setDoInput(true);
    conn.setRequestMethod(httpMethod);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
    conn.setReadTimeout(HTTP_CONNECT_TIMEOUT);

    final String statusMsg  = conn.getResponseMessage();
    final int    statusCode = conn.getResponseCode();
    
    return new ConsulResponseFacade() {
      @Override
      public String getStatusMessage() {
        return statusMsg;
      }
      
      @Override
      public int getStatusCode() {
        return statusCode;
      }
      
      @Override
      public void disconnect() {
        conn.disconnect();
      }
    };
  }
  
  ConsulResponseFacade doInvokeUrl(URL url, String httpMethod) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(false);
    conn.setRequestMethod(httpMethod);
    conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
    conn.setReadTimeout(HTTP_CONNECT_TIMEOUT);

    final String statusMsg  = conn.getResponseMessage();
    final int    statusCode = conn.getResponseCode();
    
    return new ConsulResponseFacade() {
      @Override
      public String getStatusMessage() {
        return statusMsg;
      }
      
      @Override
      public int getStatusCode() {
        return statusCode;
      }
      
      @Override
      public void disconnect() {
        conn.disconnect();
      }
    };
  }
  
  // ==========================================================================
  
  /**
   * Hides the details of the actual underlying HTTP response implementation (meant for allowing unit testing).
   *
   * @author yduchesne
   *
   */
  public interface ConsulResponseFacade {
    
    /**
     * @return a status code.
     */
    public int getStatusCode();
    
    /**
     * @return a status message.
     */
    public String getStatusMessage();
    
    /**
     * Disconnects the underlying HTTP connection.
     */
    public void disconnect();
  }
}
