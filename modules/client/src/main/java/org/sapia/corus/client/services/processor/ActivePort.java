/*
 * ActivePort.java
 *
 * Created on October 18, 2005, 11:04 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.services.port.PortRange;

/**
 * This class models an "active" port: a port that is currently being used by a
 * process.
 * 
 * @author yduchesne
 */
public class ActivePort implements Externalizable {

  static final long serialVersionUID = 1L;

  private String name;
  private int port;
  private transient String portValue;

  /**
   * Meant for externalization only.
   */
  public ActivePort() {
  }
  
  /**
   * @param name the name of the port range to which this instance corresponds.
   * @param port a port assigned to the process to which this instance is associated.
   */
  public ActivePort(String name, int port) {
    this.name = name;
    this.port = port;
  }
  
  /**
   * @return the name of the {@link PortRange} to which  this instance
   * corresponds.
   */
  public String getName() {
    return name;
  }

  /**
   * @return this instance's port value.
   */
  public int getPort() {
    return port;
  }
  
  /**
   * @param criteria a {@link PortCriteria}.
   * @return true if the given criteria match this instance.
   */
  public boolean matches(PortCriteria criteria) {
    return criteria.getPort().matches(portValue()) && criteria.getRange().matches(getName());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  public String toString() {
    return new StringBuffer(name).append(':').append(port).toString();
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(name, port);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ActivePort) {
      ActivePort other = (ActivePort) obj;
      return name.equals(other.name) && port == other.port;
    }
    return false;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    name = in.readUTF();
    port = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeInt(port);
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  public String portValue() {
    if (portValue == null) {
      portValue = Integer.toString(port);
    }
    return portValue;
  }

}
