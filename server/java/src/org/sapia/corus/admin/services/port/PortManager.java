/*
 * PortManager.java
 *
 * Created on October 18, 2005, 7:27 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.admin.services.port;

import java.util.List;

import org.sapia.corus.admin.Module;
import org.sapia.corus.exceptions.PortActiveException;
import org.sapia.corus.exceptions.PortRangeConflictException;
import org.sapia.corus.exceptions.PortRangeInvalidException;
import org.sapia.corus.exceptions.PortUnavailableException;

/**
 * This interface specifies port management behavior.
 *
 * @author yduchesne
 */
public interface PortManager extends java.rmi.Remote, Module{
  
  public static final String ROLE = PortManager.class.getName();
  
  /**
   * @param name the name of the port range to add.
   * @param min the lowerbound port.
   * @param max the higherbound port.
   */
  public void addPortRange(String name, int min, int max) throws 
    PortRangeInvalidException, PortRangeConflictException;

  /**
   * @param name the name of the port range to remove.
   * @param force if <code>true</code>, indicates that the port range should 
   * be removed even if this instance has corresponding ports flagged as
   * active.
   */
  public void removePortRange(String name, boolean force) throws PortActiveException;
  
  /**
   * Forces the release of all ports corresponding to the given name.
   *
   * @param name the name of a port range.
   */
  public void releasePortRange(String name);
  
  /**
   * @return the <code>List</code> of port ranges that this instance holds.
   */
  public List<PortRange> getPortRanges(); 
  
  /**
   * @param the name of a port range.
   * @return a port.
   */
  public int aquirePort(String name) throws PortUnavailableException;
  
  /**
   * @param the name of a port range.
   * @param port a port that was acquired from this instance.
   */ 
  public void releasePort(String name, int port);
  
}
