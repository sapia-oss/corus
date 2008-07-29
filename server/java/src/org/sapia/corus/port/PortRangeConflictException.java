/*
 * PortRangeConflictException.java
 *
 * Created on October 18, 2005, 9:41 AM
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
public class PortRangeConflictException extends Exception{
  
  /** Creates a new instance of PortRangeConflictException */
  public PortRangeConflictException(String msg) {
    super(msg);
  }
  
}
