package org.sapia.corus.client.services.deployer;

import static org.sapia.corus.client.common.ArgMatchers.any;
import static org.sapia.corus.client.common.ArgMatchers.anyIfNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;

/**
 * Holds criteria for selecting distributions.
 * 
 * @author yduchesne
 */
public class DistributionCriteria implements Externalizable {

  static final long serialVersionUID = 1L;

  private ArgMatcher name, version;
  private int        backup = 0;

  /**
   * Meant for externalization only.
   */
  public DistributionCriteria() {
  }
  
  /**
   * @see {@link #name}
   */
  public ArgMatcher getName() {
    return name;
  }

  /**
   * @see {@link #version}
   */
  public ArgMatcher getVersion() {
    return version;
  }

  /**
   * @return the number of backup distributions to keep upon undeploy.
   */
  public int getBackup() {
    return backup;
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  // --------------------------------------------------------------------------
  // Externalizables
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    name    = (ArgMatcher) in.readObject();
    version = (ArgMatcher) in.readObject();
    backup  = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeObject(version);
    out.writeInt(backup);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  public String toString() {
    return new ToStringBuilder(this).append("name", name).append("version", version).toString();
  }

  // ==========================================================================
  // Builder class
  
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
    
    public Builder copy(DistributionCriteria other) {
      name    = other.name;
      version = other.version;
      backup  = other.backup;
      return this;
    }
    
    public DistributionCriteria all() {
      return name(any()).version(any()).build();
    }

    public DistributionCriteria build() {
      DistributionCriteria criteria = new DistributionCriteria();
      criteria.name    = anyIfNull(name);
      criteria.version = anyIfNull(version);
      criteria.backup  = backup;
      return criteria;
    }
  }
}
