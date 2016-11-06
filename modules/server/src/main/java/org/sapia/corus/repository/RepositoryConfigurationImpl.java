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
  private boolean pushSecurityConfigEnabeld     = true;
  private boolean pullSecurityConfigEnabled     = true;
  private boolean bootExecEnabled               = true;
  private boolean repoServerExecProcessEnabled  = false;
  private boolean repoClientDeployScriptEnabled = true;

  private int distributionDiscoveryIntervalSeconds = DEFAULT_DIST_DISCO_INTERVAL_SECONDS;
  private int distributionDiscoveryMaxAttempts     = DEFAULT_DIST_DISCO_MAX_ATTEMPTS;
  private int maxConcurrentDeploymentRequests      = DEFAULT_MAX_CONCURRENT_DEPLOYMENT_REQUESTS;
  private int repoFileTtlMinutes                   = DEFAULT_REPO_FILE_TTL_MINUTES;
  private int repoFileCheckIntervalSeconds         = DEFAULT_REPO_FILE_CHECK_INTERVAL_SECONDS;
  private TimeRange bootstrapDelay                 = DEFAULT_BOOTSTRAP_DELAY;
  
  private long artifactDeploymentRequestActivityDelaySeconds = DEFAULT_ARTIFACT_DEPLOYMENT_DELAY_SECONDS;
  
  private long artifactDeploymentRequestActivityTimeoutSeconds = DEFAULT_ARTIFACT_DEPLOYMENT_TIMEOUT_SECONDS;
  
  public void setRepoServerExecProcessEnabled(boolean repoServerExecProcessEnabled) {
    this.repoServerExecProcessEnabled = repoServerExecProcessEnabled;
  }
  
  @Override
  public boolean isRepoServerExecProcessEnabled() {
    return repoServerExecProcessEnabled;
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
    return pushSecurityConfigEnabeld;
  }
  
  public void setPushSecurityConfigEnabled(boolean pushSecurityConfigEnabeld) {
    this.pushSecurityConfigEnabeld = pushSecurityConfigEnabeld;
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
