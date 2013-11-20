package org.sapia.corus.client.services.deployer;

import static org.sapia.corus.client.common.ArgFactory.any;
import static org.sapia.corus.client.common.ArgFactory.anyIfNull;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;

/**
 * Holds criteria for selecting distributions.
 * 
 * @author yduchesne
 */
public class DistributionCriteria implements Serializable {

  static final long serialVersionUID = 1L;

  private Arg name, version;

  /**
   * @see {@link #name}
   */
  public Arg getName() {
    return name;
  }

  /**
   * @see {@link #name}
   */
  public void setName(Arg name) {
    this.name = name;
  }

  /**
   * @see {@link #version}
   */
  public Arg getVersion() {
    return version;
  }

  /**
   * @see {@link #version}
   */
  public void setVersion(Arg version) {
    this.version = version;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("version", version).toString();
  }

  // //////////// Builder class

  public static final class Builder {

    private Arg name, version;

    private Builder() {
    }

    public Builder name(Arg name) {
      this.name = name;
      return this;
    }

    public Builder name(String name) {
      return name(ArgFactory.parse(name));
    }

    public Builder version(Arg version) {
      this.version = version;
      return this;
    }

    public Builder version(String version) {
      return version(ArgFactory.parse(version));
    }

    public DistributionCriteria all() {
      return name(any()).version(any()).build();
    }

    public DistributionCriteria build() {
      DistributionCriteria criteria = new DistributionCriteria();
      criteria.setName(anyIfNull(name));
      criteria.setVersion(anyIfNull(version));
      return criteria;
    }
  }
}
