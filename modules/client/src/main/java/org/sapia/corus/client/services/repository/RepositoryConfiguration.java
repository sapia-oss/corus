package org.sapia.corus.client.services.repository;

import java.rmi.Remote;

import org.sapia.ubik.util.TimeRange;

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
   * @return <code>true</code> if the repo server should send security configuration to
   *         repo clients (<code>true</code> by default).
   */
  public boolean isPushSecurityConfigEnabled();

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
   * @return <code>true</code> if the repo client should accept security configuration
   * from repo servers (<code>true</code> by default).
   */
  public boolean isPullSecurityConfigEnabled();
  
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
  
  /**
   * @return the interval at which the task deleting "old" repo files is run.
   */
  public int getRepoFileCheckIntervalSeconds();
  
  /**
   * @return the number of minutes corresponding to the time-to-live of repo files (i.e.: distributions, images, etc.)
   * that are cached locally.
   */
  public int getRepoFileTtlMinutes();
  
  /**
   * @return the time range within to pick a random time to wait for, after startup, to start discovering repo servers 
   * (this delay is observed by repo client nodes).
   */
  public TimeRange getBootstrapDelay();

  /**
   * @return the amount of time to wait after the time at which the last artifact deployment request has been registered.
   */
  public long getArtifactDeploymentRequestActivityDelaySeconds();
  
  /**
   * @return the maximum amount of time to wait for after the last artifact deployment request has been registered.
   */
  public long getArtifactDeploymentRequestWaitTimeoutSeconds();
  
  /**
   * @return <code>true</code> if process execution at repo server nodes should be enabled, <code>false</code>
   * otherwise.
   */
  public boolean isRepoServerExecProcessEnabled();
  
  /**
   * @return <code>true</code> if the repo server is meant to accept being synchronized by peers.
   */
  public boolean isRepoServerSyncEnabled();
 
  /**
   * @return <code>true</code> if packaged deployment scripts are to be executed at repo client nodes.
   */
  public boolean isRepoClientDeployScriptEnabled();
  
  /**
   * @return the time interval at which the state check task should run.
   */
  public int getCheckStateIntervalSeconds();
  
  /**
   * @return <code>true</code> if the state checking is enabled.
   */
  public boolean isCheckStateEnabled();

  /**
   * @return the maximum number of random hosts to check from when a node without distributions is
   *         checking other nodes for the presence of artifacts, and no repo server nodes are found.
   *         (when no repo server nodes are found, the current node will check randomly selected peers,
   *         up to the number specified by this property).
   */
  public int getCheckStateMaxRandomHosts();
  
  /**
   * @return <code>true</code> if checking random Corus nodes when no repo servers are found
   *         is enabled - <code>false</code> otherwise.
   */
  public boolean isCheckStateRandomHostsEnabled();
  
  /**
   * @return <code>true</code> if state checking should be determined automatically.
   */
  public boolean isCheckStateAutomatic();
}
