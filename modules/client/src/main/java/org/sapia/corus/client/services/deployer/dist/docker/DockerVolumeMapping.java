package org.sapia.corus.client.services.deployer.dist.docker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.*;

/**
 * Holds volume mapping configuration.
 * 
 * @author yduchesne
 *
 */
public class DockerVolumeMapping implements Externalizable, ObjectCreationCallback {

  private String hostVolume, containerVolume;
  private OptionalValue<String> permission = OptionalValue.none();

  public void setHostVolume(String hostVolume) {
    this.hostVolume = hostVolume;
  }
  
  public String getHostVolume() {
    return hostVolume;
  }
  
  public void setContainerVolume(String containerVolume) {
    this.containerVolume = containerVolume;
  }
  
  public String getContainerVolume() {
    return containerVolume;
  }
  
  public void setPermission(OptionalValue<String> permission) {
    this.permission = permission;
  }
  
  public OptionalValue<String> getPermission() {
    return permission;
  }
  
  @Override
  public Object onCreate() throws ConfigurationException {
    attributeNotNullOrEmpty("volumeMapping", "hostVolume", hostVolume);
    attributeNotNullOrEmpty("containerMapping", "containerVolume", containerVolume);
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    hostVolume      = in.readUTF();
    containerVolume = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(hostVolume);
    out.writeUTF(containerVolume);
  }
  
}
