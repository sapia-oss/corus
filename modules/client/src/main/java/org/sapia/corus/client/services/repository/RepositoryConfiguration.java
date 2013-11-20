package org.sapia.corus.client.services.repository;

import java.rmi.Remote;

/**
 * Holds repository configuration.
 * 
 * @author yduchesne
 * 
 */
public interface RepositoryConfiguration extends Remote {

  /**
   * @return <code>true</code> if the repo server should send tags to repo
   *         clients (<code>true</code> by default).
   */
  public boolean isPushTagsEnabled();

  /**
   * @return <code>true</code> if the repo server should send properties to repo
   *         clients (<code>true</code> by default).
   */
  public boolean isPushPropertiesEnabled();

  /**
   * @return <code>true</code> if the repo server should send shell scripts to
   *         repo clients (<code>true</code> by default).
   */
  public boolean isPushScriptsEnabled();

  /**
   * @return <code>true</code> if the repo server should send uploaded files to
   *         repo clients (<code>true</code> by default).
   */
  public boolean isPushFilesEnabled();

  /**
   * @return <code>true</code> if the repo server should send port ranges to
   *         repo clients (<code>true</code> by default).
   */
  public boolean isPushPortRangesEnabled();

  /**
   * @return <code>true</code> if the repo client should accept tags from repo
   *         servers (<code>true</code> by default).
   */
  public boolean isPullTagsEnabled();

  /**
   * @return <code>true</code> if the repo client should accept properties from
   *         repo servers (<code>true</code> by default).
   */
  public boolean isPullPropertiesEnabled();

  /**
   * @return <code>true</code> if the repo client should accept shell scripts
   *         from repo servers (<code>true</code> by default).
   */
  public boolean isPullScriptsEnabled();

  /**
   * @return <code>true</code> if the repo client should accept uploaded files
   *         from repo servers (<code>true</code> by default).
   */
  public boolean isPullFilesEnabled();

  /**
   * @return <code>true</code> if the repo client should accept port ranges from
   *         repo servers (<code>true</code> by default).
   */
  public boolean isPullPortRangesEnabled();

  /**
   * @return <code>true</code> if process configs that have their
   *         <code>startOnBoot</code> flag to true should have their processes
   *         automatically started after a pull or push has been completed
   *         (defaults to <code>true</code>).
   */
  public boolean isBootExecEnabled();

  /**
   * @return the number of seconds between distribution discovery attempts.
   */
  public int getDistributionDiscoveryIntervalSeconds();

  /**
   * @return the maximum number of distribution discovery attempts.
   */
  public int getDistributionDiscoveryMaxAttempts();

  /**
   * @return the maximum concurrent deployment requests that will be handled.
   */
  public int getMaxConcurrentDeploymentRequests();

}
