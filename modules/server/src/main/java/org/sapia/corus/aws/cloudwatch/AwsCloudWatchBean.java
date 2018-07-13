package org.sapia.corus.aws.cloudwatch;

import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.aws.AwsConfiguration.AwsConfigChangeListener;
import org.sapia.corus.client.annotations.VisibleForTests;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.StringWriterJsonStream;
import org.sapia.corus.client.services.event.CorusEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.ubik.util.Strings;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEvents;
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsClientBuilder;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequest;
import com.amazonaws.services.cloudwatchevents.model.PutEventsRequestEntry;

/**
 * Consumes {@link CorusEvent}s and dispatches them to AWS' CloudWatch.
 * 
 * @author yduchesne
 *
 */
public class AwsCloudWatchBean {
  
  @Autowired 
  private ServerContext                   serverContext;
  
  @Autowired
  private EventDispatcher                 dispatcher;
  
  @Autowired
  private AwsConfiguration                awsConfiguration;

  @Autowired
  private InternalConfigurator            configurator;
  
  private OptionalValue<String>           region            = OptionalValue.none();
  
  private volatile AmazonCloudWatchEvents client;

  private DynamicProperty<Boolean>        isEnabled         = new DynamicProperty<Boolean>(false);

  private Object lock = new Object();

  // --------------------------------------------------------------------------
  // Visible for testing

  DynamicProperty<Boolean> getIsEnabled() {
    return isEnabled;
  }

  OptionalValue<String> getRegion() {
    return region;
  }

  AmazonCloudWatchEvents getClient() {
    return client;
  }

  void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }

  void setAwsConfiguration(AwsConfiguration awsConfiguration) {
    this.awsConfiguration = awsConfiguration;
  }

  void setDispatcher(EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  void setServerContext(ServerContext serverContext) {
    this.serverContext = serverContext;
  }

  // --------------------------------------------------------------------------
  // Config setters
  
  public void setRegion(String region) {
    if (region != null && !Strings.isBlank(region)) {
      this.region = OptionalValue.of(region);
    }
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = new DynamicProperty<Boolean>(isEnabled);
  }

  // --------------------------------------------------------------------------
  // Lifecycle
 
  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_AWS_CLOUDWATCH_ENABLED, isEnabled);

    isEnabled.addListener(property -> {

      if (property.getValueNotNull() && isActuallyEnabled()) {
        createClient();
      } else if (client != null) {
        shutdownClient();
      }
    });

    awsConfiguration.addConfigChangeListener(new AwsConfigChangeListener() {
      @Override
      public void onAwsEnabled() {
        if (isActuallyEnabled()) {
          createClient();
        }
      }
      
      @Override
      public void onAwsDisabled() {
        if (client != null) {
          shutdownClient();
        }
      }
    });
    
    if (isActuallyEnabled()) {
      createClient();
    }
    
    dispatcher.addInterceptor(CorusEvent.class, this);
  }
  
  @PreDestroy
  public void destroy() {
    shutdownClient();
  }
  
  // --------------------------------------------------------------------------
  // Event interceptor method
  
  public void onCorusEvent(CorusEvent event) {
    // normally, client should not be null if AWS integration is enabled
    // (and should be null if it isn't), but desynchronization may occur
    // due to AWS integration being disabled by another thread through a 
    // Corus server property change. Since this is a rare event, we avoid
    // using locks and prefer checking state.
    if (client != null && isActuallyEnabled()) {
      
      // taking this precaution since client can be shut down and set to 
      // null by another thread after the above null check
      AmazonCloudWatchEvents tmp = client;
      if (tmp != null) {
        StringWriterJsonStream stream = new StringWriterJsonStream();
        event.toJson(serverContext.getCorusHost(), stream);
        
        PutEventsRequestEntry entry = new PutEventsRequestEntry()
            .withTime(event.getTime())
            .withSource(event.getSource())
            .withDetailType(event.getType())
            .withDetail(stream.toString());
        
        tmp.putEvents(
            new PutEventsRequest()
              .withEntries(Collections.singleton(entry))
        );
      }
    }
  }

  // --------------------------------------------------------------------------
  // Restricted

  @VisibleForTests
  protected AmazonCloudWatchEvents doCreateClient(OptionalValue<String> theRegion) {
    return AmazonCloudWatchEventsClientBuilder.standard()
        .withRegion(theRegion.get())
        .build();
  }

  private void createClient() {
    synchronized (lock) {
      if (client == null) {
        client = doCreateClient(region);
      }
    }
  }

  private void shutdownClient() {
    synchronized (lock) {
      if (client != null) {
        client.shutdown();
        client = null;
      }
    }
  }

  private boolean isActuallyEnabled() {
    return awsConfiguration.isAwsEnabled() && isEnabled.getValue();
  }

}
