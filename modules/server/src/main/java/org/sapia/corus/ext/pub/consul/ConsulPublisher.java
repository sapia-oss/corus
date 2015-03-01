package org.sapia.corus.ext.pub.consul;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.http.HttpResponseFacade;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.sapia.corus.taskmanager.core.Task;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Publishes a service description corresponding to this Corus node to the configured Consul agent.
 * 
 * @author yduchesne
 *
 */
public class ConsulPublisher extends ModuleHelper {
  
  public static final String ROLE = ConsulPublisher.class.getName();
  
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
  // Module interface
  
  @Override
  public String getRoleName() {
    return ROLE;
  }
  
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
          startPublishTask();
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
    startPublishTask();
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private synchronized void startPublishTask() {
    if (publishTask == null && publishingEnabled.getValueNotNull()) {
      publishTask = new Task<Void, Void>("ConsulPublishTask") {
        public Void execute(org.sapia.corus.taskmanager.core.TaskExecutionContext ctx, Void param) throws Throwable 
        {
          if (publishingEnabled.getValueNotNull()) {
            doPublish();
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
  
  private void doPublish() {
    logger().info("Publishing to Consul is enabled - proceeding");
    StringWriter sw = new StringWriter();
    JsonStream stream = new WriterJsonStream(sw);
    Endpoint ep = serverContext().getCorusHost().getEndpoint();
    stream
      .beginObject()
        .field("ID").value("corus-" + serverContext().getCorusHost().getFormattedAddress())
        .field("Name").value("Corus")
        .field("Tags").strings(new String[] {
            serverContext().getCorus().getDomain()
         })
        .field("Address").value(ep.getServerTcpAddress().getHost())
        .field("Port").value(ep.getServerTcpAddress().getPort())
        .field("Checks").beginArray()
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
  
  ConsulResponseFacade doInvokeUrl(URL url) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(false);
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT);

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
