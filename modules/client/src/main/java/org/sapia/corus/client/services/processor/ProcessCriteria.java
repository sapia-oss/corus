package org.sapia.corus.client.services.processor;

import static org.sapia.corus.client.common.ArgMatchers.any;
import static org.sapia.corus.client.common.ArgMatchers.anyIfNull;
import static org.sapia.corus.client.common.ArgMatchers.parse;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.ubik.util.Collects;

/**
 * Holds criteria for selecting processes.
 * 
 * @author yduchesne
 */
public class ProcessCriteria implements Externalizable {

  static final long serialVersionUID = 1L;

  /**
   * Corresponds to the process name.
   */
  private ArgMatcher name;

  /**
   * Corresponds to the process id.
   */
  private ArgMatcher pid;
  
  /**
   * Corresponds to the process OS pid.
   */
  private ArgMatcher osPid;
  
  /**
   * Corresponds to the process profile.
   */
  private OptionalValue<String> profile;

  /**
   * Corresponds to the process distribution.
   */
  private ArgMatcher distribution;
  
  /**
   * Holds the {@link LifeCycleStatus}es to match.
   */
  private Set<Process.LifeCycleStatus> lifeCycles = new HashSet<Process.LifeCycleStatus>();
  
  private OptionalValue<PortCriteria> portCriteria = OptionalValue.of(null);

  /**
   * Corresponds to the process version.
   */
  private ArgMatcher version;
  
  /**
   * Do not use: meant for externalization only.
   */
  public ProcessCriteria() {
  }
  
  public ArgMatcher getName() {
    return name;
  }

  public ArgMatcher getPid() {
    return pid;
  }
  
  public ArgMatcher getOsPid() {
    return osPid;
  }

  public OptionalValue<String> getProfile() {
    return profile;
  }

  public ArgMatcher getDistribution() {
    return distribution;
  }

  public ArgMatcher getVersion() {
    return version;
  }
  
  public Set<Process.LifeCycleStatus> getLifeCycles() {
    return lifeCycles;
  }
  
  public OptionalValue<PortCriteria> getPortCriteria() {
    return portCriteria;
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

  // --------------------------------------------------------------------------
  // Object override
  
  public String toString() {
    return new ToStringBuilder(this)
        .append("distribution", distribution)
        .append("name", name)
        .append("version", version)
        .append("profile", profile)
        .toString();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    name         = (ArgMatcher) in.readObject();
    pid          = (ArgMatcher) in.readObject();
    osPid        = (ArgMatcher) in.readObject();
    profile      = (OptionalValue<String>) in.readObject();
    distribution = (ArgMatcher) in.readObject();
    lifeCycles   = (Set<Process.LifeCycleStatus>) in.readObject();
    version      = (ArgMatcher) in.readObject();
    portCriteria = (OptionalValue<PortCriteria>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeObject(pid);
    out.writeObject(osPid);
    out.writeObject(profile);
    out.writeObject(distribution);
    out.writeObject(lifeCycles);
    out.writeObject(version);
    out.writeObject(portCriteria);
  }
  
  // ==========================================================================
  
  /**
   * Builder class.
   * 
   * @author yduchesne
   *
   */
  public static final class Builder {
    
    private ArgMatcher name, distribution, version, pid, osPid;
    private OptionalValue<String>       profile = OptionalValue.none();
    private OptionalValue<PortCriteria> ports   = OptionalValue.none();
    private Set<LifeCycleStatus>        lifeCycles;

    private Builder() {
    }

    public Builder name(ArgMatcher name) {
      this.name = name;
      return this;
    }

    public Builder name(String name) {
      return name(parse(name));
    }

    public Builder pid(ArgMatcher pid) {
      this.pid = pid;
      return this;
    }
    
    public Builder osPid(ArgMatcher osPid) {
      this.osPid = osPid;
      return this;
    }

    public Builder profile(String profile) {
      this.profile = OptionalValue.of(profile);
      return this;
    }

    public Builder distribution(ArgMatcher dist) {
      this.distribution = dist;
      return this;
    }

    public Builder distribution(String dist) {
      return distribution(parse(dist));
    }

    public Builder version(ArgMatcher version) {
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
    
    public Builder ports(PortCriteria ports) {
      this.ports = OptionalValue.of(ports);
      return this;
    }
    
    public Builder copy(ProcessCriteria c) {
      this.distribution = c.distribution;
      this.lifeCycles   = c.lifeCycles;
      this.name         = c.name;
      this.pid          = c.pid;
      this.profile      = c.profile;
      this.version      = c.version;
      this.ports         = c.portCriteria;
      return this;
    }

    public ProcessCriteria all() {
      ProcessCriteria criteria = new ProcessCriteria();
      criteria.distribution = any();
      criteria.name         = any();
      criteria.version      = any();
      criteria.pid          = any();
      criteria.osPid        = any();
      criteria.profile      = OptionalValue.none();
      criteria.portCriteria = OptionalValue.none();
      return criteria;
    }

    public ProcessCriteria build() {
      ProcessCriteria criteria = new ProcessCriteria();
      criteria.distribution = anyIfNull(distribution);
      criteria.name         = anyIfNull(name);
      if (profile != null) {
        criteria.profile = profile;
      }
      criteria.version = anyIfNull(version);
      criteria.pid     = anyIfNull(pid);
      criteria.osPid   = anyIfNull(osPid);
      if (lifeCycles != null) {
        criteria.lifeCycles = lifeCycles;
      }
      if (ports != null) {
        criteria.portCriteria = ports;
      }
      return criteria;
    }
  }
}
