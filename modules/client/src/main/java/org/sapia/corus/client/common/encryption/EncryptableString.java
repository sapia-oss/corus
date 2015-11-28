package org.sapia.corus.client.common.encryption;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Assertions;

/**
 * Implementation of the {@link Encryptable} interface, meant to 
 * provide incrypting/decrypting behavior around strings.
 * 
 * @author yduchesne
 *
 */
public class EncryptableString implements Encryptable<String>, Externalizable {

  private String decrypted;
  private byte[] encrypted;

  /**
   * DO NOT CALL: meant for externalization only.
   */
  public EncryptableString() {
  }
  
  private EncryptableString(String decrypted) {
    this.decrypted = decrypted;
  }
  
  private EncryptableString(byte[] encrypted) {
    this.encrypted = encrypted;
  }
  
  // --------------------------------------------------------------------------
  // Construction
  
  /**
   * @param value the {@link String} that the returned instance will wrap.
   * @return a new {@link Encryptable}, wrapping the given string.
   */
  public static Encryptable<String> of(String value) {
    return new EncryptableString(value);
  }
  
  // --------------------------------------------------------------------------
  // Encryptable interface
  
  @Override
  public OptionalValue<String> getValue() {
    return OptionalValue.of(decrypted);
  }
  
  @Override
  public boolean isDecrypted() {
    return decrypted != null;
  }
  
  @Override
  public boolean isEncrypted() {
    return encrypted != null;
  }
  
  @Override
  public Encryptable<String> decrypt(DecryptionContext ctx) {
    if (decrypted == null) {
      try {
        byte[] decryptedBytes = ctx.getCipher().doFinal(encrypted);
        String decrypted = new String(decryptedBytes);
        return new EncryptableString(decrypted);
      } catch (Exception e) {
        throw new IllegalStateException("Error performing decryption", e);
      }
    }
    return this;
  }

  @Override
  public Encryptable<String> encrypt(EncryptionContext ctx) {
    if (encrypted == null) {
      try {
        byte[] encryptedBytes = ctx.getCipher().doFinal(decrypted.getBytes());
        Assertions.illegalState(encryptedBytes.length == 0, "Encrypted payload size is 0");
        return new EncryptableString(encryptedBytes);
      } catch (Exception e) {
        throw new IllegalStateException("Error performing encryption", e);
      }
    }
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    decrypted = (String) in.readObject();
    encrypted = (byte[]) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(decrypted);
    out.writeObject(encrypted);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  /**
   * Warning: if the given instance is an {@link EncryptableString} which is encrypted, and this
   * one is also encrypted, then their encrypte byte payload will be compared for equality. If both
   * payloads have not been generated with the same public key, then this test might yield <code>true</code>
   * when in fact the actual decrypted strings are different.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EncryptableString) {
      EncryptableString other = (EncryptableString) obj;
      if (decrypted != null && other.decrypted != null) {
        return ObjectUtils.safeEquals(decrypted, other.decrypted);
      } else if (encrypted != null && other.encrypted != null) {
        return ObjectUtils.safeEquals(encrypted, other.encrypted);
      }
      return false;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (decrypted != null) {
      return decrypted.hashCode();
    }
    int hashCode = 0;
    for (int i = 0; i < encrypted.length; i++) {
      hashCode += encrypted[i] * 31;
    }
    return hashCode;
  }

}
