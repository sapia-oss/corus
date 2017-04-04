package org.sapia.corus.client.facade;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;

/**
 * This interface specifies a facade to the Corus {@link PortManager}
 * 
 * @author yduchesne
 * 
 */
public interface PortManagementFacade {

  /**
   * @param name
   *          the name of the port range to add.
   * @param min
   *          the lowerbound port of the range.
   * @param max
   *          the higherbound port of the range.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void addPortRange(String name, int min, int max, ClusterInfo cluster) throws PortRangeConflictException, PortRangeInvalidException;

  /**
   * @param ranges
   *          a {@link List} of {@link PortRange}
   * @param clearExisting
   *          if <code>true</code>, indicates that the existing port ranges must
   *          be deleted.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void addPortRanges(List<PortRange> ranges, boolean clearExisting, ClusterInfo cluster) throws PortRangeConflictException,
      PortRangeInvalidException;

  /**
   * @param name
   *          the name of the port range to add.
   * @param force
   *          if <code>true</code>, indicates that the port range should be
   *          removed even if the Corus server has corresponding ports flagged
   *          as active.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   */
  public void removePortRange(String name, boolean force, ClusterInfo cluster) throws PortActiveException;

  /**
   * Returns the {@link PortRange} instances that hold the pre-configured ports
   * of the specified Corus servers.
   * 
   * @param cluster a {@link ClusterInfo} instance.
   */
  public Results<List<PortRange>> getPortRanges(ClusterInfo cluster);
  
  /**
   * Archives currently set port ranges.
   * 
   * @param revId the revision ID to use.
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void archive(RevId revId, ClusterInfo cluster);
  
  /**
   * Unarchives currently set port ranges.
   * 
   * @param revId the revision ID to use.
   * @param cluster a {@link ClusterInfo} instance.
   */
  public void unarchive(RevId revId, ClusterInfo cluster);

}
