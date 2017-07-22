package org.sapia.corus.aws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.annotations.VisibleForTests;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.sapia.ubik.util.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.util.EC2MetadataUtils;

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
  
  private OptionalValue<String>         region       = OptionalValue.none();
  
  private OptionalValue<String>         availZone    = OptionalValue.none();

  private List<AwsConfigChangeListener> listeners    = Collections.synchronizedList(new ArrayList<AwsConfigChangeListener>());
  
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  DynamicProperty<Boolean> getIsAwsEnabled() {
    return isAwsEnabled;
  }
  
  void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }
  
  public void setInstanceId(OptionalValue<String> instanceId) {
    this.instanceId = instanceId;
  }
  
  public void setRegion(OptionalValue<String> region) {
    this.region = region;
  }
  
  public void setAvailabilityZone(OptionalValue<String> availZone) {
    this.availZone = availZone;
  }
  
  // --------------------------------------------------------------------------
  // Config setters
  
  public void setAwsEnabled(boolean isAwsEnabled) {
    this.isAwsEnabled = new DynamicProperty<Boolean>(isAwsEnabled);
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
              l.onAwsDisabled();
            }
          }
        }
      }
    });
    
    if (isAwsEnabled.getValue()) {
      log.info("AWS support enabled");
    } else {
      log.info("AWS support disabled");
    }
  }

  // --------------------------------------------------------------------------
  // AwsConfiguration interface
  
  @Override
  public String getInstanceId() throws IllegalStateException {
    checkAwsEnabled();
    instanceId.ifNull(() -> instanceId = OptionalValue.of(retrieveInstanceId()));
    return instanceId.get();
  }
  
  @Override
  public String getRegion() throws IllegalStateException {
    checkAwsEnabled();
    region.ifNull(() -> region = OptionalValue.of(retrieveRegion()));
    return region.get();
  }
  
  @Override
  public String getAvailabilityZone() throws IllegalStateException {
    checkAwsEnabled();
    availZone.ifNull(() -> availZone = OptionalValue.of(retrieveAvailabilityZone()));
    return availZone.get();  
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
  
  
  @VisibleForTests
  protected String retrieveInstanceId() {
    String toReturn = EC2MetadataUtils.getInstanceId();
    Assertions.notNull(toReturn, "EC2 instance ID could not be determined");
    return toReturn;
  }
  
  @VisibleForTests
  protected String retrieveAvailabilityZone() {
    String toReturn = EC2MetadataUtils.getAvailabilityZone();
    Assertions.notNull(toReturn, "AWS availability zone could not be determined");
    return toReturn;
  }
  
  @VisibleForTests
  protected String retrieveRegion() {
    String toReturn = EC2MetadataUtils.getEC2InstanceRegion();
    Assertions.notNull(toReturn, "AWS region could not be determined");
    return toReturn;
  }
  
  private void checkAwsEnabled() throws IllegalStateException {
    if (!isAwsEnabled()) {
      throw new IllegalStateException("AWS support disabled");
    }
  }
  

}
