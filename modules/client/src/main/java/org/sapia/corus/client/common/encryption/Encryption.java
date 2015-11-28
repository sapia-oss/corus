package org.sapia.corus.client.common.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * Provides encryption-related utility methods.
 * 
 * @author yduchesne
 *
 */
public class Encryption {
  
  public static final String DEFAULT_KEY_ALGO    = "RSA";
  public static final int    DEFAULT_KEY_LEN     = 2048;
  public static final String DEFAULT_CIPHER_ALGO = "RSA/ECB/PKCS1Padding";
  
  public Encryption() {
  }
  
  /**
   * @return a new, default {@link KeyPair}.
   */
  public static KeyPair generateDefaultKeyPair() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(DEFAULT_KEY_ALGO);
      kpg.initialize(DEFAULT_KEY_LEN);
      return kpg.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Could not generate default keypair", e);
    }
  }
  
  /**
   * @param pubKey a {@link PublicKey}.
   * @return a new {@link EncryptionContext}.
   */
  public static EncryptionContext getDefaultEncryptionContext(final PublicKey pubKey) {
    return getEncryptionContext(pubKey, DEFAULT_CIPHER_ALGO);
  }
  
  /**
   * @param pubKey a {@link PublicKey}.
   * @param cipherAlgo the name of a cipher algorithm.
   * @return a new {@link EncryptionContext}.
   */
  public static EncryptionContext getEncryptionContext(final PublicKey pubKey, final String cipherAlgo) {
    return new EncryptionContext() {
      
      @Override
      public PublicKey getPublicKey() {
        return pubKey;
      }
      
      @Override
      public Cipher getCipher() {
        try {
          Cipher cipher = Cipher.getInstance(cipherAlgo);
          cipher.init(Cipher.ENCRYPT_MODE, pubKey);
          return cipher;
        } catch (Exception e) {
          throw new IllegalStateException("Could not create cipher", e);
        }
      }
    };
  }
  
  /**
   * @param privKey a {@link PrivateKey}.
   * @return a new {@link DecryptionContext}.
   */
  public static DecryptionContext getDefaultDecryptionContext(final PrivateKey privKey) {
    return getDecryptionContext(privKey, DEFAULT_CIPHER_ALGO);
  }

  /**
   * @param privKey a {@link PrivateKey}.
   * @param cipherAlgo the name of a cipher algorithm.
   * @return a new {@link DecryptionContext}.
   */
  public static DecryptionContext getDecryptionContext(final PrivateKey privKey, final String cipherAlgo) {
    return new DecryptionContext() {
      
      @Override
      public PrivateKey getPrivateKey() {
        return privKey;
      }
      
      @Override
      public Cipher getCipher() {
        try {
          Cipher cipher = Cipher.getInstance(cipherAlgo);
          cipher.init(Cipher.DECRYPT_MODE, privKey);
          return cipher;
        } catch (Exception e) {
          throw new IllegalStateException("Could not create cipher", e);
        }
      }
    };
    
  }
}
