package org.sapia.corus.client.services.processor;

import static org.sapia.corus.client.common.ArgFactory.any;
import static org.sapia.corus.client.common.ArgFactory.anyIfNull;
import static org.sapia.corus.client.common.ArgFactory.parse;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.deployer.DistributionCriteria;

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
    return new ToStringBuilder(this).append("distribution", distribution).append("name", name).append("version", version).append("profile", profile)
        .toString();
  }

  // //////////// Builder class

  public static final class Builder {
    private Arg name, distribution, version, pid;
    private String profile;

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
      criteria.setProfile(profile);
      criteria.setVersion(anyIfNull(version));
      criteria.setPid(anyIfNull(pid));
      return criteria;
    }
  }
}
