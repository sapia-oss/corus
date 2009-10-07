package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.exceptions.DuplicateDistributionException;
import org.sapia.corus.exceptions.LogicException;

public interface DistributionDatabase {

  /**
   * Adds a distribution to this instance.
   * 
   * @param dist a <code>Distribution</code>.
   * @throws DuplicateDistributionException
   */
  public abstract void addDistribution(Distribution dist)
      throws DuplicateDistributionException;

  /**
   * Tests for the presence of a distribution within this instance.
   * 
   * @see Distribution
   * 
   * @param name the name of a distribution.
   * @param version the version of a distribution.
   * @return <code>true</code> if this instance contains the distribution correponding
   * to the given name and version.
   */
  public abstract boolean containsDistribution(Arg name,
      Arg version);

  /**
   * @param name a distribution name.
   * @param version a distribution version.
   * @return
   */
  public abstract boolean containsDistribution(String name, String version);

  /**
   * Removes the distribution that matches the given parameters from this instance.
   * 
   * @see Distribution
   * 
   * @param name the name of a distribution.
   * @param version the version of a distribution.
   * @return <code>true</code> if this instance contains the distribution correponding
   * to the given name and version.
   */
  public abstract void removeDistribution(Arg name, Arg version);

  /**
   * Returns a list of distributions.
   * 
   * @return a <code>List</code> of <code>Distribution</code>s.
   */
  public abstract List<Distribution> getDistributions();

  /**
   * Returns a list of distributions that match the given distribution
   * name.
   * 
   * @param name the name of the distribution.
   * @return a <code>List</code> of <code>Distribution</code>s.
   */
  public abstract List<Distribution> getDistributions(Arg name);

  /**
   * Returns a list of distributions that match the given distribution
   * name and version.
   * 
   * @param name the name of the distribution.
   * @param version the version of the distribution.
   * @return a <code>List</code> of <code>Distribution</code>s.
   */
  public abstract List<Distribution> getDistributions(Arg name,
      Arg version);

  /**
   * Returns the distribution that matches the given distribution
   * name and version.
   * 
   * @param name the name of the distribution.
   * @param version the version of the distribution.
   * 
   * @return a <code>Distribution</code>.
   */
  public abstract Distribution getDistribution(Arg name,
      Arg version) throws LogicException;

}