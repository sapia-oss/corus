package org.sapia.corus.client.common.encryption;

import org.sapia.corus.client.common.OptionalValue;

/**
 * Specifies the behavior for encrypting/decrypting.
 * 
 * @author yduchesne
 *
 */
public interface Encryptable<T> {

  /**
   * @param ctx the {@link EncryptionContext} to use for encryption.
   * @return a new, encrypted copy of this instance.
   */
  public Encryptable<T> encrypt(EncryptionContext ctx);
  
  /**
   * @param ctx the {@link DecryptionContext} to use for decryption.
   * @return a new, decrypted copy of this instance.
   */
  public Encryptable<T> decrypt(DecryptionContext ctx);
 
  /**
   * @return <code>true</code> of this instance's value is encrypted.
   */
  public boolean isEncrypted();
  
  /**
   * @return <code>true</code> of this instance's value is decrypted.
   */
  public boolean isDecrypted();
  
  /**
   * @return this instance's value, which will not be set of this instance
   * is in the encrypted state.
   */
  public OptionalValue<T> getValue();
}
