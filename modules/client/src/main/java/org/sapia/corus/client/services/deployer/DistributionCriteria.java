package org.sapia.corus.client.services.deployer;

import static org.sapia.corus.client.common.ArgMatchers.any;
import static org.sapia.corus.client.common.ArgMatchers.anyIfNull;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * Holds criteria for selecting distributions.
 * 
 * @author yduchesne
 */
public class DistributionCriteria implements Serializable {

  static final long serialVersionUID = 1L;

  private ArgMatcher name, version;
  private int     backup = 0;

  /**
   * @see {@link #name}
   */
  public ArgMatcher getName() {
    return name;
  }

  /**
   * @see {@link #name}
   */
  public void setName(ArgMatcher name) {
    this.name = name;
  }

  /**
   * @see {@link #version}
   */
  public ArgMatcher getVersion() {
    return version;
  }

  /**
   * @see {@link #version}
   */
  public void setVersion(ArgMatcher version) {
    this.version = version;
  }
 
  /**
   * @return the number of backup distributions to keep upon undeploy.
   */
  public int getBackup() {
    return backup;
  }
  
  /**
   * @see #getBackup()
   */
  public void setBackup(int backup) {
    this.backup = backup;
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("version", version).toString();
  }

  // //////////// Builder class

  public static final class Builder {

    private ArgMatcher name, version;
    private int backup;

    private Builder() {
    }

    public Builder name(ArgMatcher name) {
      this.name = name;
      return this;
    }

    public Builder name(String name) {
      return name(ArgMatchers.parse(name));
    }

    public Builder version(ArgMatcher version) {
      this.version = version;
      return this;
    }

    public Builder version(String version) {
      return version(ArgMatchers.parse(version));
    }
    
    public Builder backup(int backup) {
      this.backup = backup;
      return this;
    }
    
    public DistributionCriteria all() {
      return name(any()).version(any()).build();
    }

    public DistributionCriteria build() {
      DistributionCriteria criteria = new DistributionCriteria();
      criteria.setName(anyIfNull(name));
      criteria.setVersion(anyIfNull(version));
      criteria.setBackup(backup);
      return criteria;
    }
  }
}
