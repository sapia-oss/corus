package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Strings;

/**
 * Holds different deployment-related flags.
 * 
 * @author yduchesne
 *
 */
public class DeployPreferences implements Externalizable {

  static final long serialVersionUID = 1L;
  
  private boolean execDeployScripts;
  private OptionalValue<ChecksumPreference> checksum = OptionalValue.none();
 
  /**
   * Public/empty ctor defined explicitely as a reminder that it is
   * a requirement for externalization.
   * 
   * @see Externalizable
   */
  public DeployPreferences() {
  }
  
  /**
   * Indicates that the deploy scripts in the distribution should be executed.
   * 
   * @return this instance.
   */
  public DeployPreferences executeDeployScripts() {
    this.execDeployScripts = true;
    return this;
  }
  
  /**
   * @param execDeployScripts if <code>true</code>, indicates that the deploy scripts in the distribution 
   * should be executed.
   * @return this instance.
   */
  public DeployPreferences setExecDeployScripts(boolean execDeployScripts) {
    this.execDeployScripts = execDeployScripts;
    return this;
  }
  
  /**
   * @return <code>true</code> if the deploy scripts should be executed.
   */
  public boolean isExecuteDeployScripts() {
    return execDeployScripts;
  }

  /**
   * @param cs the {@link ChecksumPreference} to verify on the Corus server side.
   * @return this instance.
   */
  public DeployPreferences setChecksum(ChecksumPreference cs) {
    this.checksum = OptionalValue.of(cs);
    return this;
  }
 
  /**
   * @return this instance's optional {@link ChecksumPreference}.
   */
  public OptionalValue<ChecksumPreference> getChecksum() {
    return checksum;
  }
  
  /**
   * @return a new instance of this class.
   */
  public static DeployPreferences newInstance() {
    return new DeployPreferences();
  }
  
  /**
   * @return a new {@link DeployPreferences} instance.
   */
  public DeployPreferences getCopy() {
    DeployPreferences copy = new DeployPreferences();
    copy.checksum          = checksum;
    copy.execDeployScripts = execDeployScripts;
    return copy;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toString("execDeployScripts", execDeployScripts, "checksum", checksum);
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(execDeployScripts, checksum);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DeployPreferences) {
      DeployPreferences other = DeployPreferences.class.cast(obj);
      return ObjectUtil.safeEquals(this.checksum, other.checksum)
      && this.execDeployScripts && other.execDeployScripts;
    }
    return false;
  }

  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.execDeployScripts = in.readBoolean();
    this.checksum = (OptionalValue<ChecksumPreference>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeBoolean(execDeployScripts);
    out.writeObject(checksum);
  }
  
}
