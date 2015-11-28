package org.sapia.corus.client.common.encryption;

import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * Specifies the behavior of an encryption context, expected to provide the objects 
 * required for encryption.
 * 
 * @author yduchesne
 *
 */
public interface EncryptionContext {

  /**
   * @return this instance's {@link PublicKey}.
   */
  PublicKey getPublicKey();
  
  /**
   * @return a new {@link Cipher} for the corresponding public key.
   */
  Cipher getCipher();
  
}
