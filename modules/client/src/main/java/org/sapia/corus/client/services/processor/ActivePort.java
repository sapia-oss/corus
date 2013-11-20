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

import java.io.Serializable;

/**
 * This class models an "active" port: a port that is currently being used by a
 * process.
 * 
 * @author yduchesne
 */
public class ActivePort implements Serializable {

  static final long serialVersionUID = 1L;

  private String name;
  private int port;

  /** Creates a new instance of ActivePort */
  public ActivePort(String name, int port) {
    this.name = name;
    this.port = port;
  }

  public String getName() {
    return name;
  }

  public int getPort() {
    return port;
  }

  public String toString() {
    return new StringBuffer(name).append('/').append(port).toString();
  }

}
