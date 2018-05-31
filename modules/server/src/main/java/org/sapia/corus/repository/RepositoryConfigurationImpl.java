package org.sapia.corus.repository;

import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.ubik.util.TimeRange;
import org.sapia.ubik.util.TimeValue;

/**
 * Implementation of the {@link RepositoryConfiguration} interface.
 * 
 * @author yduchesne
 * 
 */
public class RepositoryConfigurationImpl implements RepositoryConfiguration {

  static final long serialVersionUID = 1L;

  private static final int DEFAULT_MAX_CONCURRENT_DEPLOYMENT_REQUESTS   = 3;
  private static final int DEFAULT_DIST_DISCO_INTERVAL_SECONDS          = 5;
  private static final int DEFAULT_DIST_DISCO_MAX_ATTEMPTS              = 3;
  private static final int DEFAULT_REPO_FILE_TTL_MINUTES                = 60;
  private static final int DEFAULT_REPO_FILE_CHECK_INTERVAL_SECONDS     = 120;
  private static final int DEFAULT_ARTIFACT_DEPLOYMENT_DELAY_SECONDS    = 5;
  private static final int DEFAULT_ARTIFACT_DEPLOYMENT_TIMEOUT_SECONDS  = 15;
  private static final int DEFAULT_CHECK_STATE_INTERVAL_SECONDS         = 60;
  private static final int DEFAULT_CHECK_STATE_MAX_RANDOM_HOSTS         = 5;

  private static final TimeRange DEFAULT_BOOTSTRAP_DELAY = new TimeRange(TimeValue.createSeconds(5), TimeValue.createSeconds(10));

  private boolean pushTagsEnabled               = true;
  private boolean pullTagsEnabled               = true;
  private boolean pushScriptsEnabled            = true;
  private boolean pullScriptsEnabled            = true;
  private boolean pushFilesEnabled              = true;
  private boolean pullFilesEnabled              = true;
  private boolean pushPropertiesEnabled         = true;
  private boolean pullPropertiesEnabled         = true;
  private boolean pushPortRangesEnabled         = true;
  private boolean pullPortRangesEnabled         = true;
  private boolean pushSecurityConfigEnabled     = true;
  private boolean pullSecurityConfigEnabled     = true;
  private boolean bootExecEnabled               = true;
  private boolean repoServerExecProcessEnabled  = false;
  private boolean repoClientDeployScriptEnabled = true;
  private boolean repoServerSyncEnabled         = false;
  private boolean checkStateEnabled             = true;
  private boolean checkStateRandomHostsEnabled  = false;
  private boolean checkStateAutomatic           = true;

  private int distributionDiscoveryIntervalSeconds = DEFAULT_DIST_DISCO_INTERVAL_SECONDS;
  private int distributionDiscoveryMaxAttempts     = DEFAULT_DIST_DISCO_MAX_ATTEMPTS;
  private int maxConcurrentDeploymentRequests      = DEFAULT_MAX_CONCURRENT_DEPLOYMENT_REQUESTS;
  private int repoFileTtlMinutes                   = DEFAULT_REPO_FILE_TTL_MINUTES;
  private int repoFileCheckIntervalSeconds         = DEFAULT_REPO_FILE_CHECK_INTERVAL_SECONDS;
  private int checkStateIntervalSeconds            = DEFAULT_CHECK_STATE_INTERVAL_SECONDS;
  private int checkStateMaxRandomHosts             = DEFAULT_CHECK_STATE_MAX_RANDOM_HOSTS;
  private TimeRange bootstrapDelay                 = DEFAULT_BOOTSTRAP_DELAY;
  
  
  private long artifactDeploymentRequestActivityDelaySeconds = DEFAULT_ARTIFACT_DEPLOYMENT_DELAY_SECONDS;
  
  private long artifactDeploymentRequestActivityTimeoutSeconds = DEFAULT_ARTIFACT_DEPLOYMENT_TIMEOUT_SECONDS;

  
  public void setCheckStateEnabled(boolean checkStateEnabled) {
    this.checkStateEnabled = checkStateEnabled;
  }
  
  @Override
  public boolean isCheckStateEnabled() {
    return checkStateEnabled;
  }
  
  public void setCheckStateAutomatic(boolean checkStateAutomatic) {
    this.checkStateAutomatic = checkStateAutomatic;
  }
  
  @Override
  public boolean isCheckStateAutomatic() {
    return checkStateAutomatic;
  }
  
  public void setCheckStateRandomHostsEnabled(boolean checkStateRandomHostsEnabled) {
    this.checkStateRandomHostsEnabled = checkStateRandomHostsEnabled;
  }
  
  @Override
  public boolean isCheckStateRandomHostsEnabled() {
    return checkStateRandomHostsEnabled;
  }
  
  public void setCheckStateIntervalSeconds(int checkStateIntervalSeconds) {
    this.checkStateIntervalSeconds = checkStateIntervalSeconds;
  }
  
  @Override
  public int getCheckStateIntervalSeconds() {
    return checkStateIntervalSeconds;
  }
  
  public void setCheckStateMaxRandomHosts(int checkStateMaxRandomHosts) {
    this.checkStateMaxRandomHosts = checkStateMaxRandomHosts;
  }
  
  @Override
  public int getCheckStateMaxRandomHosts() {
    return checkStateMaxRandomHosts;
  }
  
  public void setRepoServerExecProcessEnabled(boolean repoServerExecProcessEnabled) {
    this.repoServerExecProcessEnabled = repoServerExecProcessEnabled;
  }
    
  @Override
  public boolean isRepoServerExecProcessEnabled() {
    return repoServerExecProcessEnabled;
  }
  
  public void setRepoServerSyncEnabled(boolean repoServerSyncEnabled) {
    this.repoServerSyncEnabled = repoServerSyncEnabled;
  }
  
  @Override
  public boolean isRepoServerSyncEnabled() {
    return repoServerSyncEnabled;
  }
  
  public void setRepoClientDeployScriptEnabled(boolean repoClientDeployScriptEnabled) {
    this.repoClientDeployScriptEnabled = repoClientDeployScriptEnabled;
  }
  
  @Override
  public boolean isRepoClientDeployScriptEnabled() {
    return repoClientDeployScriptEnabled;
  }
  
  public void setArtifactDeploymentRequestActivityDelaySeconds(long artifactDeploymentRequestActivityDelaySeconds) {
    this.artifactDeploymentRequestActivityDelaySeconds = artifactDeploymentRequestActivityDelaySeconds;
  }
  
  @Override
  public long getArtifactDeploymentRequestActivityDelaySeconds() {
    return artifactDeploymentRequestActivityDelaySeconds;
  }
  
  public void setArtifactDeploymentRequestActivityTimeoutSeconds(long artifactDeploymentRequestActivityTimeoutSeconds) {
    this.artifactDeploymentRequestActivityTimeoutSeconds = artifactDeploymentRequestActivityTimeoutSeconds;
  }
  
  @Override
  public long getArtifactDeploymentRequestWaitTimeoutSeconds() {
    return this.artifactDeploymentRequestActivityTimeoutSeconds;
  }
  
  public void setRepoFileCheckIntervalSeconds(int repoFileCheckIntervalSeconds) {
    this.repoFileCheckIntervalSeconds = repoFileCheckIntervalSeconds;
  }
  
  @Override
  public int getRepoFileCheckIntervalSeconds() {
    return repoFileCheckIntervalSeconds;
  }
  
  public void setRepoFileTtlMinutes(int repoFileTtlMinutes) {
    this.repoFileTtlMinutes = repoFileTtlMinutes;
  }
  
  @Override
  public TimeRange getBootstrapDelay() {
    return bootstrapDelay;
  }
  
  public void setBootstrapDelay(String bootstrapDelayLiteral) {
    this.bootstrapDelay = TimeRange.valueOf(bootstrapDelayLiteral);
  }
  
  @Override
  public int getRepoFileTtlMinutes() {
    return repoFileTtlMinutes;
  }
  
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
  // Security
  
  @Override
  public boolean isPushSecurityConfigEnabled() {
    return pushSecurityConfigEnabled;
  }
  
  public void setPushSecurityConfigEnabled(boolean pushSecurityConfigEnabled) {
    this.pushSecurityConfigEnabled = pushSecurityConfigEnabled;
  }
  
  @Override
  public boolean isPullSecurityConfigEnabled() {
    return pullSecurityConfigEnabled;
  }
  
  public void setPullSecurityConfigEnabled(boolean pullSecurityConfigEnabled) {
    this.pullSecurityConfigEnabled = pullSecurityConfigEnabled;
  }

  // --------------------------------------------------------------------------
  // others

  public void setDistributionDiscoveryIntervalSeconds(int distributionDiscoveryIntervalSeconds) {
    this.distributionDiscoveryIntervalSeconds = distributionDiscoveryIntervalSeconds;
  }

  @Override
  public int getDistributionDiscoveryIntervalSeconds() {
    return distributionDiscoveryIntervalSeconds;
  }

  public void setDistributionDiscoveryMaxAttempts(int distributionDiscoveryMaxAttempts) {
    this.distributionDiscoveryMaxAttempts = distributionDiscoveryMaxAttempts;
  }

  @Override
  public int getDistributionDiscoveryMaxAttempts() {
    return distributionDiscoveryMaxAttempts;
  }

  public void setMaxConcurrentDeploymentRequests(int maxConcurrentDeploymentRequests) {
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
