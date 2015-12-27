package org.sapia.corus.cloud;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

/**
 * Holds Corus-specific user data.
 * 
 * @author yduchesne
 * 
 */
public class CorusUserData {
  
  /**
   * Models an artifact, corresponding to a distribution to deploy.
   * 
   * @author yduchesne
   *
   */
  public static class Artifact {
    
    private String  url;
    
    public Artifact(String url) {
      this.url        = url;
    }
    
    public String getUrl() {
      return url;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Artifact) {
        Artifact other = (Artifact) obj;
        return ObjectUtil.safeEquals(url, other.url);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      return ObjectUtil.safeHashCode(url);
    }
  }
  
  // ==========================================================================

  private Properties serverProperties       = new Properties();
  private Properties processProperties      = new Properties();
  private Set<String> serverTags            = new HashSet<String>();
  private Set<Artifact> artifacts           = new HashSet<Artifact>();
  
  private OptionalValue<String>   domain    = OptionalValue.none();
  private OptionalValue<RepoRole> repoRole  = OptionalValue.none();
  
  /**
   * @return this instance's server {@link Properties}.
   */
  public Properties getServerProperties() {
    return serverProperties;
  }

  /**
   * @return this instance's process {@link Properties}.
   */
  public Properties getProcessProperties() {
    return processProperties;
  }

  /**
   * @return this instance's {@link Set} of server tags.
   */
  public Set<String> getServerTags() {
    return serverTags;
  }
  
  /**
   * @return this instance's {@link Set} of {@link Artifact}s, corresponding to distributions
   * to deploy at startup.
   */
  public Set<Artifact> getArtifacts() {
    return artifacts;
  }
  
  public void setRepoRole(RepoRole repoRole) {
    this.repoRole = OptionalValue.of(repoRole);
  }
  
  public void setDomain(String domain) {
    this.domain = OptionalValue.of(domain);
  }
 
  /**
   * @return the domain name to assign to the Corus server.
   */
  public OptionalValue<String> getDomain() {
    return domain;
  }
  
  /**
   * @return the {@link RepoRole} to assign to the Corus server.
   */
  public OptionalValue<RepoRole> getRepoRole() {
    return repoRole;
  }
 
}
