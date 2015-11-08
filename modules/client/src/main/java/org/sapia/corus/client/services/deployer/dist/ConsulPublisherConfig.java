package org.sapia.corus.client.services.deployer.dist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Strings;

/**
 * Holds the configuration for publish processes to Consul.
 * 
 * @author yduchesne
 *
 */
public class ConsulPublisherConfig implements ProcessPubConfig, Externalizable {

  private static final int DEFAULT_CHECK_INTERVAL = 15;
  
  private static final int DEFAULT_CHECK_TIMEOUT  = 3;
  
  private OptionalValue<String> serviceName = OptionalValue.none();
  
  private int checkInterval = DEFAULT_CHECK_INTERVAL; 
  
  private int checkTimeout  = DEFAULT_CHECK_TIMEOUT;
  
  public void setServiceName(String serviceName) {
    this.serviceName = OptionalValue.of(serviceName);
  }
  
  public OptionalValue<String> getServiceName() {
    return serviceName;
  }
  
  public void setCheckInterval(int checkInterval) {
    this.checkInterval = checkInterval;
  }

  public int getCheckInterval() {
    return checkInterval;
  }
  
  public void setCheckTimeout(int checkTimeout) {
    this.checkTimeout = checkTimeout;
  }
  
  public int getCheckTimeout() {
    return checkTimeout;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, 
        "checkInterval", checkInterval, 
        "checkTimeout", checkTimeout, 
        "serviceName", serviceName
    );
  }
  
  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(checkInterval, checkTimeout, serviceName);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConsulPublisherConfig) {
      ConsulPublisherConfig other = (ConsulPublisherConfig) obj;
      return ObjectUtils.safeEquals(checkInterval, other.checkInterval)
          && ObjectUtils.safeEquals(checkTimeout, other.checkTimeout)
          && ObjectUtils.safeEquals(serviceName, other.serviceName);
    }
    return false;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
 
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    serviceName   = (OptionalValue<String>) in.readObject();
    checkInterval = in.readInt();
    checkTimeout  = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(serviceName);
    out.writeInt(checkInterval);
    out.writeInt(checkTimeout);
  }
 
}
