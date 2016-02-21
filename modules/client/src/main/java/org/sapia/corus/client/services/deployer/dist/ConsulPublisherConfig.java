package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Strings;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Holds the configuration for publish processes to Consul.
 * 
 * @author yduchesne
 *
 */
public class ConsulPublisherConfig implements ProcessPubConfig, Externalizable, ObjectCreationCallback {

  public static final String ELEMENT_NAME = "consul-publisher";
  
  private static final int DEFAULT_CHECK_INTERVAL = 15;
  
  private static final int DEFAULT_CHECK_TIMEOUT  = 3;
  
  private OptionalValue<String> serviceName = OptionalValue.none();
  
  private String checkInterval = Integer.toString(DEFAULT_CHECK_INTERVAL); 
  
  private String checkTimeout  = Integer.toString(DEFAULT_CHECK_TIMEOUT);
  
  public void setServiceName(String serviceName) {
    this.serviceName = OptionalValue.of(serviceName);
  }
  
  public OptionalValue<String> getServiceName() {
    return serviceName;
  }
  
  public void setCheckInterval(String value) {
    this.checkInterval = value;
  }

  public String getCheckInterval() {
    return checkInterval;
  }
  
  public void setCheckTimeout(String value) {
    this.checkTimeout = value;
  }
  
  public String getCheckTimeout() {
    return checkTimeout;
  }

  // --------------------------------------------------------------------------
  // ObjectCreationCallBack
  
  @Override
  public Object onCreate() throws ConfigurationException {
    attributeNotNullOrEmpty(ELEMENT_NAME, "checkInterval", checkInterval);
    attributeNotNullOrEmpty(ELEMENT_NAME, "checkTimeout", checkTimeout);
    return this;
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
    return ObjectUtil.safeHashCode(checkInterval, checkTimeout, serviceName);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConsulPublisherConfig) {
      ConsulPublisherConfig other = (ConsulPublisherConfig) obj;
      return ObjectUtil.safeEquals(checkInterval, other.checkInterval)
          && ObjectUtil.safeEquals(checkTimeout, other.checkTimeout)
          && ObjectUtil.safeEquals(serviceName, other.serviceName);
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
    checkInterval = in.readUTF();
    checkTimeout  = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(serviceName);
    out.writeUTF(checkInterval);
    out.writeUTF(checkTimeout);
  }
 
}
