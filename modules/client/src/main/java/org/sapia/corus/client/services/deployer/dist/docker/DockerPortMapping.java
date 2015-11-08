package org.sapia.corus.client.services.deployer.dist.docker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DockerPortMapping implements Externalizable {

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
