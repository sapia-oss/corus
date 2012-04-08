package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Specifies the behavior for persisting and retrieving {@link Distribution}s.
 * 
 * @author yduchesne
 *
 */
public interface DistributionDatabase {

  /**
   * Adds a distribution to this instance.
   * 
   * @param dist a {@link Distribution}.
   * @throws DuplicateDistributionException
   */
  public void addDistribution(Distribution dist)
      throws DuplicateDistributionException;

  /**
   * Tests for the presence of a distribution within this instance.
   * 
   * @see Distribution
   * 
   * @param criteria a {@link DistributionCriteria}
   * @return <code>true</code> if this instance contains the distribution corresponding
   * to the given criteria.
   */
  public boolean containsDistribution(DistributionCriteria criteria);

  /**
   * Removes the distribution that matches the given criteria.
   * 
   * @see Distribution
   * 
   * @param criteria a {@link DistributionCriteria}
   * @param version an {@link Arg} corresponding to the version of a distribution.
   */
  public void removeDistribution(DistributionCriteria criteria);

  /**
   * Returns a list of distributions that match the given distribution
   * criteria.
   * 
   * @param criteria a {@link DistributionCriteria}
   * @return a {@link List} of {@link Distribution}s.
   */
  public List<Distribution> getDistributions(DistributionCriteria criteria);

  /**
   * Returns the distribution that matches the given distribution
   * criteria.
   * 
   * @param criteria a {@link DistributionCriteria}
   * @return a {@link Distribution}.
   */
  public abstract Distribution getDistribution(DistributionCriteria criteria) throws DistributionNotFoundException;

}