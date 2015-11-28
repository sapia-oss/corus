package org.sapia.corus.client.common.encryption;

import java.security.PrivateKey;

import javax.crypto.Cipher;

/**
 * Specifies the behavior of an encryption context, expected to provide the objects 
 * required for encryption.
 * 
 * @author yduchesne
 */
public interface DecryptionContext {

  /**
   * @return this instance's {@link PrivateKey}.
   */
  PrivateKey getPrivateKey();
  
  /**
   * @return a new {@link Cipher} for the corresponding private key.
   */
  Cipher getCipher();
}
