/*
 * PortManager.java
 *
 * Created on October 18, 2005, 7:27 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.client.services.port;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;

/**
 * This interface specifies port management behavior.
 * 
 * @author yduchesne
 */
public interface PortManager extends java.rmi.Remote, Module {

  public static final String ROLE = PortManager.class.getName();

  /**
   * @param name
   *          the name of the port range to add.
   * @param min
   *          the lowerbound port.
   * @param max
   *          the higherbound port.
   */
  public void addPortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException;

  /**
   * @param name
   *          the name of the port range to update.
   * @param min
   *          the lowerbound port.
   * @param max
   *          the higherbound port.
   */
  public void updatePortRange(String name, int min, int max) throws PortRangeInvalidException, PortRangeConflictException;

  /**
   * @param ranges
   *          a {@link List} of {@link PortRange}s.
   * @param clearExisting
   *          if <code>true</code>, indicates that the existing port ranges
   *          should be deleted.
   * @throws PortRangeInvalidException
   * @throws PortRangeConflictException
   */
  public void addPortRanges(List<PortRange> ranges, boolean clearExisting) throws PortRangeInvalidException, PortRangeConflictException;

  /**
   * @param name
   *          the name of the port range to remove.
   * @param force
   *          if <code>true</code>, indicates that the port range should be
   *          removed even if this instance has corresponding ports flagged as
   *          active.
   */
  public void removePortRange(Arg name, boolean force) throws PortActiveException;

  /**
   * Forces the release of all ports corresponding to the given name.
   * 
   * @param name
   *          the name of a port range.
   */
  public void releasePortRange(Arg name);

  /**
   * @return the {@link List} of {@link PortRange}s that this instance holds.
   */
  public List<PortRange> getPortRanges();

  /**
   * @param name
   *          the name of a port range.
   * @return a port.
   */
  public int aquirePort(String name) throws PortUnavailableException;

  /**
   * @param name
   *          the name of a port range.
   * @param port
   *          a port that was acquired from this instance.
   */
  public void releasePort(String name, int port);

}
