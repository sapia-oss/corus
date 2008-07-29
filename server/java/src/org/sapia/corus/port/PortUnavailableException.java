/*
 * PortUnavailableException.java
 *
 * Created on October 18, 2005, 9:44 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.port;

/**
 *
 * @author yduchesne
 */
public class PortUnavailableException extends Exception{
  
  /** Creates a new instance of PortUnavailableException */
  public PortUnavailableException(String msg) {
    super(msg);
  }
  
}
