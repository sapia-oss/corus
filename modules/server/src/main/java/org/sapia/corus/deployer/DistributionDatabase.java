package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public interface DistributionDatabase {

  /**
   * Adds a distribution to this instance.
   * 
   * @param dist a {@link Distribution}.
   * @throws DuplicateDistributionException
   */
  public abstract void addDistribution(Distribution dist)
      throws DuplicateDistributionException;

  /**
   * Tests for the presence of a distribution within this instance.
   * 
   * @see Distribution
   * 
   * @param name an {@link Arg} corresponding to the name of a distribution.
   * @param version an {@link Arg} corresponding to the version of a distribution.
   * @return <code>true</code> if this instance contains the distribution corresponding
   * to the given name and version.
   */
  public abstract boolean containsDistribution(Arg name,
      Arg version);

  /**
   * @param name a distribution name.
   * @param version a distribution version.
   * @return <code>true</code> if this instance contains a distribution with the
   * given name and version.
   */
  public abstract boolean containsDistribution(String name, String version);

  /**
   * Removes the distribution that matches the given parameters from this instance.
   * 
   * @see Distribution
   * 
   * @param name an {@link Arg} corresponding to the name of a distribution.
   * @param version an {@link Arg} corresponding to the version of a distribution.
   */
  public abstract void removeDistribution(Arg name, Arg version);

  /**
   * Returns a list of distributions.
   * 
   * @return a {@link List} of {@link Distribution}s.
   */
  public abstract List<Distribution> getDistributions();

  /**
   * Returns a list of distributions that match the given distribution
   * name.
   * 
   * @param name an {@link Arg} corresponding to the name of the distribution.
   * @return a {@link List} of {@link Distribution}s.
   */
  public abstract List<Distribution> getDistributions(Arg name);

  /**
   * Returns a list of distributions that match the given distribution
   * name and version.
   * 
   * @param name an {@link Arg} corresponding to the name of the distribution.
   * @param version an {@link Arg} corresponding to the version of the distribution.
   * @return a {@link List} of {@link Distribution}s.
   */
  public abstract List<Distribution> getDistributions(Arg name,
      Arg version);

  /**
   * Returns the distribution that matches the given distribution
   * name and version.
   * 
   * @param name an {@link Arg} corresponding to the name of the distribution.
   * @param version an {@link Arg} corresponding to the version of the distribution.
   * 
   * @return a {@link Distribution}.
   */
  public abstract Distribution getDistribution(Arg name,
      Arg version) throws DistributionNotFoundException;

}