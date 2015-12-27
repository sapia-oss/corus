package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Strings;

/**
 * Holds a checksum value, and an indicator specifying which algorithm has been used
 * to compute that checksum.
 * 
 * @author yduchesne
 *
 */
public class ChecksumPreference implements Externalizable {

  /**
   * Holds constants corresponding to the different checksum algorithms.
   */
  public enum ChecksumAlgo {
    MD5
  }
  
  // ==========================================================================
  
  private ChecksumAlgo algo;
  private OptionalValue<String> clientChecksum = OptionalValue.none();
 
  /**
   * DO NOT USE: meant for externalization only
   */
  public ChecksumPreference() {
  }
  
  public ChecksumPreference(ChecksumAlgo algo) {
    this.algo  = algo;
  }
  
  /**
   * @return this instance's {@link ChecksumAlgo}, corresponding to the algorithm used to
   * compute its checksum value.
   */
  public ChecksumAlgo getAlgo() {
    return algo;
  }
  
  /**
   * @param checksumValue the client-side checksum, provided for validation on the server-side.
   * @return this instance.
   */
  public ChecksumPreference assignClientChecksum(String checksumValue) {
    clientChecksum = OptionalValue.of(checksumValue);
    return this;
  }
  
  /**
   * @return this instance's checksum value.
   */
  public OptionalValue<String> getClientChecksum() {
    return clientChecksum;
  }

  // --------------------------------------------------------------------------
  // Factory methods
  
  /**
   * @param value a MD5 checksum value, passed as string.
   * @return a new instance of this class.
   */
  public static ChecksumPreference forMd5() {
    return new ChecksumPreference(ChecksumAlgo.MD5);
  }

  // --------------------------------------------------------------------------
  // Object overriddes
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "algo", algo, "clientCheckSum", clientChecksum);
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(algo, clientChecksum);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ChecksumPreference) {
      ChecksumPreference other = (ChecksumPreference) obj;
      if (!algo.equals(other.algo)) {
        return false;
      } else if (clientChecksum.isNull() && other.clientChecksum.isNull()) {
        return true;
      } else if (clientChecksum.isNull() || other.clientChecksum.isNull()) {
        return false;
      }
      return clientChecksum.get().equalsIgnoreCase(other.getClientChecksum().get());
    }
    return false;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    clientChecksum = (OptionalValue<String>) in.readObject();
    algo = (ChecksumAlgo) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(clientChecksum);
    out.writeObject(algo);
  }
  
}
