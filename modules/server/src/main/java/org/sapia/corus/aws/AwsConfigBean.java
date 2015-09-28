package org.sapia.corus.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

/**
 * Implementation of the {@link AwsConfiguration} interface.
 * 
 * @author yduchesne
 *
 */
public class AwsConfigBean implements AwsConfiguration {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(AwsConfigBean.class.getName());
  
  @Autowired
  private InternalConfigurator          configurator;
  
  private DynamicProperty<Boolean>      isAwsEnabled = new DynamicProperty<Boolean>(false);

  private OptionalValue<String>         instanceId   = OptionalValue.none();

  private List<AwsConfigChangeListener> listeners    = Collections.synchronizedList(new ArrayList<AwsConfigChangeListener>());
  
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setAwsEnabled(boolean isAwsEnabled) {
    this.isAwsEnabled = new DynamicProperty<Boolean>(isAwsEnabled);
  }
  
  void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }

  // --------------------------------------------------------------------------
  // Lifecycle
  
  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_AWS_ENABLED, isAwsEnabled);
    
    // taking into config update
    isAwsEnabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        if (property.getValue()) {
          log.info("AWS integration enabled");
          synchronized (listeners) {
            for (AwsConfigChangeListener l : listeners) {
              l.onAwsEnabled();
            }
          }
        } else {
          log.info("AWS integration disabled");
          synchronized (listeners) {
            for (AwsConfigChangeListener l : listeners) {
              l.onAwsEnabled();
            }
          }
        }
      }
    });
    
    if (isAwsEnabled.getValue()) {
      log.debug("AWS support enabled");
    } else {
      log.debug("AWS support disabled");
    }
  }

  // --------------------------------------------------------------------------
  // AwsConfiguration interface
  
  @Override
  public String getInstanceId() throws IllegalStateException {
    checkAwsEnabled();
    if (instanceId.isNull()) {
      try {
        instanceId = OptionalValue.of(retrieveInstanceId());
      } catch (IOException e) {
        throw new IllegalStateException("Could not retrieve EC2 instance ID", e);
      }
    }
    return instanceId.get();
  }
  
  @Override
  public boolean isAwsEnabled() {
    return isAwsEnabled.getValue();
  }
  
  @Override
  public AWSCredentials getCredentials() {
    checkAwsEnabled();
    return new DefaultAWSCredentialsProviderChain().getCredentials();
  }
  
  @Override
  public void addConfigChangeListener(AwsConfigChangeListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  private void checkAwsEnabled() throws IllegalStateException {
    throw new IllegalStateException("AWS support disabled");
  }
  
  private static String retrieveInstanceId() throws IOException {
    String inputLine = null;
    URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
    
    URLConnection conn = url.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    try {
      while ((inputLine = in.readLine()) != null) {
        return inputLine;
      }
    } finally {
      in.close();
    }
    throw new IllegalStateException("No EC2 instance ID data available");
  }

}
