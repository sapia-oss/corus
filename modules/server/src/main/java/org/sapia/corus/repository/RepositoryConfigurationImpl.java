package org.sapia.corus.repository;

import org.sapia.corus.client.services.repository.RepositoryConfiguration;

/**
 * Implementation of the {@link RepositoryConfiguration} interface.
 * 
 * @author yduchesne
 *
 */
public class RepositoryConfigurationImpl implements RepositoryConfiguration {
  
  static final long serialVersionUID = 1L;
  
  private static final int DEFAULT_MAX_CONCURRENT_DEPLOYMENT_REQUESTS = 3;  
  private static final int DEFAULT_DIST_DISCO_INTERVAL_SECONDS        = 5;
  private static final int DEFAULT_DIST_DISCO_MAX_ATTEMPTS            = 3;
  
  private boolean pushTagsEnabled       = true;
  private boolean pullTagsEnabled       = true;
  private boolean pushScriptsEnabled    = true;  
  private boolean pullScriptsEnabled    = true;
  private boolean pushFilesEnabled      = true;  
  private boolean pullFilesEnabled      = true;  
  private boolean pushPropertiesEnabled = true;
  private boolean pullPropertiesEnabled = true;
  private boolean pushPortRangesEnabled = true;
  private boolean pullPortRangesEnabled = true;  
  private boolean bootExecEnabled       = true;
  
  private int     distributionDiscoveryIntervalSeconds  = DEFAULT_DIST_DISCO_INTERVAL_SECONDS;
  private int     distributionDiscoveryMaxAttempts      = DEFAULT_DIST_DISCO_MAX_ATTEMPTS;
  private int     maxConcurrentDeploymentRequests       = DEFAULT_MAX_CONCURRENT_DEPLOYMENT_REQUESTS;
  
  // --------------------------------------------------------------------------
  // tags
  
  public void setPushTagsEnabled(boolean pushTagsEnabled) {
    this.pushTagsEnabled = pushTagsEnabled;
  }

  @Override
  public boolean isPushTagsEnabled() {
    return pushTagsEnabled;
  }
  
  public void setPullTagsEnabled(boolean pullTagsEnabled) {
    this.pullTagsEnabled = pullTagsEnabled;
  }

  @Override
  public boolean isPullTagsEnabled() {
    return pullTagsEnabled;
  }
  
  // --------------------------------------------------------------------------
  // properties  
  
  public void setPushPropertiesEnabled(boolean pushPropertiesEnabled) {
    this.pushPropertiesEnabled = pushPropertiesEnabled;
  }

  @Override
  public boolean isPushPropertiesEnabled() {
    return pushPropertiesEnabled;
  }
  
  public void setPullPropertiesEnabled(boolean pullPropertiesEnabled) {
    this.pullPropertiesEnabled = pullPropertiesEnabled;
  }

  @Override
  public boolean isPullPropertiesEnabled() {
    return pullPropertiesEnabled;
  }
  
  // --------------------------------------------------------------------------
  // files  
  
  public void setPullFilesEnabled(boolean pullFilesEnabled) {
    this.pullFilesEnabled = pullFilesEnabled;
  }
  
  @Override
  public boolean isPullFilesEnabled() {
    return pullFilesEnabled;
  }
  
  public void setPushFilesEnabled(boolean pushFilesEnabled) {
    this.pushFilesEnabled = pushFilesEnabled;
  }
  
  @Override
  public boolean isPushFilesEnabled() {
    return pushFilesEnabled;
  }
  
  // --------------------------------------------------------------------------
  // scripts  
 
  @Override
  public boolean isPullScriptsEnabled() {
    return pullScriptsEnabled;
  }
  
  public void setPullScriptsEnabled(boolean pullScriptsEnabled) {
    this.pullScriptsEnabled = pullScriptsEnabled;
  }
  
  @Override
  public boolean isPushScriptsEnabled() {
    return pushScriptsEnabled;
  }
  
  public void setPushScriptsEnabled(boolean pushScriptsEnabled) {
    this.pushScriptsEnabled = pushScriptsEnabled;
  }
  
  // --------------------------------------------------------------------------
  // port ranges  
  
  @Override
  public boolean isPushPortRangesEnabled() {
    return pushPortRangesEnabled;
  }
  
  public void setPushPortRangesEnabled(boolean pushPortRangesEnabled) {
    this.pushPortRangesEnabled = pushPortRangesEnabled;
  }
  
  @Override
  public boolean isPullPortRangesEnabled() {
    return pullPortRangesEnabled;
  }
  
  public void setPullPortRangesEnabled(boolean pullPortRangesEnabled) {
    this.pullPortRangesEnabled = pullPortRangesEnabled;
  }
  
  // --------------------------------------------------------------------------
  // others  
  
  public void setDistributionDiscoveryIntervalSeconds(
      int distributionDiscoveryIntervalSeconds) {
    this.distributionDiscoveryIntervalSeconds = distributionDiscoveryIntervalSeconds;
  }

  @Override
  public int getDistributionDiscoveryIntervalSeconds() {
    return distributionDiscoveryIntervalSeconds;
  }

  public void setDistributionDiscoveryMaxAttempts(
      int distributionDiscoveryMaxAttempts) {
    this.distributionDiscoveryMaxAttempts = distributionDiscoveryMaxAttempts;
  }
  
  @Override
  public int getDistributionDiscoveryMaxAttempts() {
    return distributionDiscoveryMaxAttempts;
  }
  
  public void setMaxConcurrentDeploymentRequests(
      int maxConcurrentDeploymentRequests) {
    this.maxConcurrentDeploymentRequests = maxConcurrentDeploymentRequests;
  }
  
  @Override
  public int getMaxConcurrentDeploymentRequests() {
    return maxConcurrentDeploymentRequests;
  }
  
  public void setBootExecEnabled(boolean bootExecEnabled) {
    this.bootExecEnabled = bootExecEnabled;
  }
  
  @Override
  public boolean isBootExecEnabled() {
    return bootExecEnabled;
  }

}
