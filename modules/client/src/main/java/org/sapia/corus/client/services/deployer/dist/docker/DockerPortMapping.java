package org.sapia.corus.client.services.deployer.dist.docker;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Holds port mapping configuration. 
 * 
 * @author yduchesne
 *
 */
public class DockerPortMapping implements Externalizable, ObjectCreationCallback {

  private String hostPort, containerPort;
  
  public void setHostPort(String hostPort) {
    this.hostPort = hostPort;
  }
  
  public String getHostPort() {
    return hostPort;
  }

  public void setContainerPort(String containerPort) {
    this.containerPort = containerPort;
  }
  
  public String getContainerPort() {
    return containerPort;
  }
  
  // --------------------------------------------------------------------------
  // ObjectCreationCallback interface
  
  @Override
  public Object onCreate() throws ConfigurationException {
    attributeNotNull("portMapping", "hostPort", hostPort);
    attributeNotNull("portMapping", "containerPort", containerPort);
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    hostPort      = in.readUTF();
    containerPort = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(hostPort);
    out.writeUTF(containerPort);
  }
  
}
