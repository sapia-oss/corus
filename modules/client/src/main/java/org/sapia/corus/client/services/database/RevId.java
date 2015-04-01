package org.sapia.corus.client.services.database;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Assertions;

/**
 * Models a revision ID.
 * 
 * @author yduchesne
 *
 */
public class RevId implements Externalizable {

  static final long serialVersionUID = 1L;

  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private int    classVersion = CURRENT_VERSION;  
  private String value;
  
  /**
   * Do not call: meant for externalization.
   */
  public RevId() {
  }
  
  private RevId(String value) {
    this.value = value;
  }
  
  /**
   * @return this instance's value.
   */
  public String get() {
    Assertions.illegalState(value == null, "Revision ID value not set");
    return value;
  }
  
  /**
   * @param revId a revision ID.
   * @return a new {@link RevId} instance.
   */
  public static RevId valueOf(String revId) {
    Assertions.notNull(revId, "Revision ID cannot be null");
    String theId = revId.trim();
    Assertions.isFalse(theId.length() == 0, "Revision ID cannot be empty");
    int alphaNumCount = 0;
    for (int i = 0; i < revId.length(); i++) {
      char c = revId.charAt(i);
      if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '-' && c != '_' && c != '.') {
        throw new IllegalArgumentException("Invalid revision ID: %s. Use at least one alpha-numeric character, and: -_.");
      }
      if (Character.isAlphabetic(c) || Character.isDigit(c)) {
        alphaNumCount++;
      }
    }
    if (alphaNumCount == 0) {
      throw new IllegalArgumentException("Alpha-numeric characters must be present in revision ID: " + revId);
    }
    return new RevId(revId);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RevId) {
      return ((RevId) obj).value.equals(value);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return value.hashCode();
  }
  
  @Override
  public String toString() {
    return value == null ? "null" : value;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      value = in.readUTF();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
    classVersion = CURRENT_VERSION;
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(classVersion);
    out.writeUTF(value);
  }
  
}
