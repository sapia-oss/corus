package org.sapia.corus.client.services.processor;

import static org.sapia.corus.client.common.ArgFactory.any;
import static org.sapia.corus.client.common.ArgFactory.anyIfNull;
import static org.sapia.corus.client.common.ArgFactory.parse;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.ubik.util.Collects;

/**
 * Holds criteria for selecting processes.
 * 
 * @author yduchesne
 */
public class ProcessCriteria implements Serializable {

  static final long serialVersionUID = 1L;

  /**
   * Corresponds to the process name.
   */
  private Arg name;

  /**
   * Corresponds to the process id.
   */
  private Arg pid;

  /**
   * Corresponds to the process profile.
   */
  private String profile;

  /**
   * Corresponds to the process distribution.
   */
  private Arg distribution;
  
  /**
   * Holds the {@link LifeCycleStatus}es to match.
   */
  private Set<Process.LifeCycleStatus> lifeCycles = new HashSet<Process.LifeCycleStatus>();

  /**
   * Corresponds to the process version.
   */
  private Arg version;

  public Arg getName() {
    return name;
  }

  public void setName(Arg name) {
    this.name = name;
  }

  public Arg getPid() {
    return pid;
  }

  public void setPid(Arg pid) {
    this.pid = pid;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public Arg getDistribution() {
    return distribution;
  }

  public void setDistribution(Arg distribution) {
    this.distribution = distribution;
  }

  public Arg getVersion() {
    return version;
  }
  
  public void setVersion(Arg version) {
    this.version = version;
  }
  
  public void setLifeCycles(Set<LifeCycleStatus> lifeCycles) {
    this.lifeCycles = lifeCycles;
  }
  
  public void setLifeCycles(LifeCycleStatus...lifeCycles) {
    this.lifeCycles = Collects.arrayToSet(lifeCycles);
  }
  
  public Set<Process.LifeCycleStatus> getLifeCycles() {
    return lifeCycles;
  }

  /**
   * @return a {@link DistributionCriteria} encapsulating this instance's
   *         {@link #name} and {@link #version}.
   */
  public DistributionCriteria getDistributionCriteria() {
    return DistributionCriteria.builder().name(distribution).version(version).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public String toString() {
    return new ToStringBuilder(this)
        .append("distribution", distribution)
        .append("name", name)
        .append("version", version)
        .append("profile", profile)
        .toString();
  }

  // //////////// Builder class

  public static final class Builder {
    
    private Arg name, distribution, version, pid;
    private String profile;
    private Set<LifeCycleStatus> lifeCycles;

    private Builder() {
    }

    public Builder name(Arg name) {
      this.name = name;
      return this;
    }

    public Builder name(String name) {
      return name(parse(name));
    }

    public Builder pid(Arg pid) {
      this.pid = pid;
      return this;
    }

    public Builder profile(String profile) {
      this.profile = profile;
      return this;
    }

    public Builder distribution(Arg dist) {
      this.distribution = dist;
      return this;
    }

    public Builder distribution(String dist) {
      return distribution(parse(dist));
    }

    public Builder version(Arg version) {
      this.version = version;
      return this;
    }

    public Builder version(String version) {
      return version(parse(version));
    }
    
    public Builder lifecycles(LifeCycleStatus...lifeCycles) {
      this.lifeCycles = Collects.arrayToSet(lifeCycles);
      return this;
    }

    public ProcessCriteria all() {
      ProcessCriteria criteria = new ProcessCriteria();
      criteria.setDistribution(any());
      criteria.setName(any());
      criteria.setVersion(any());
      criteria.setPid(any());
      return criteria;
    }

    public ProcessCriteria build() {
      ProcessCriteria criteria = new ProcessCriteria();
      criteria.setDistribution(anyIfNull(distribution));
      criteria.setName(anyIfNull(name));
      if (profile != null) {
        criteria.setProfile(profile);
      }
      criteria.setVersion(anyIfNull(version));
      criteria.setPid(anyIfNull(pid));
      if (lifeCycles != null) {
        criteria.setLifeCycles(this.lifeCycles);
      }
      return criteria;
    }
  }
}
