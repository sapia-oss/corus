package org.sapia.corus.client.services.audit;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.encryption.DecryptionContext;
import org.sapia.corus.client.common.encryption.EncryptionContext;
import org.sapia.ubik.util.Assertions;

/**
 * Encapsulates auditing information.
 * 
 * @author yduchesne
 *
 */
public class AuditInfo implements Externalizable {
  
  public enum TokenType {
    APP_ID,
    USERNAME;
  }
  
  private String    userToken;
  private TokenType type;
  private byte[]    encryptedToken;
  
  /**
   * DO NOT CALL: meant for externalization only.
   */
  public AuditInfo() {
  }
  
  /**
   * @param userToken a user token.
   * @param type the {@link TokenType} of the given token.
   */
  AuditInfo(String userToken, TokenType type) {
    this.userToken = userToken;
    this.type = type;
  }
  
  /**
   * @param encryptedToken the byte array corresponding to the encrypted version of a user token.
   * @param type the {@link TokenType} of the given token.
   */
  AuditInfo(byte[] encryptedToken, TokenType type) {
    this.encryptedToken = encryptedToken;
    this.type = type;
  }
  
  /**
   * @return this instance's user token.
   */
  public String getUserToken() {
    return userToken;
  }
   
  /**
   * @return this instance's {@link TokenType}.
   */
  public TokenType getType() {
    return type;
  }
  
  /**
   * @return <code>true</code> if this instance is encrypted.
   */
  public boolean isEncrypted() {
    return encryptedToken != null;
  }

  /**
   * @param clientId a client ID..
   * @return a new {@link AuditInfo}, of type {@link TokenType#APP_ID}.
   */
  public static AuditInfo forClientId(String clientId) {
    return new AuditInfo(clientId, TokenType.APP_ID);
  }
  
  /**
   * @param username a username.
   * @return a new {@link AuditInfo}, of type {@link TokenType#USERNAME}.
   */
  public static AuditInfo forUser(String username) {
    return new AuditInfo(username, TokenType.USERNAME);
  }
  
  /**
   * @return a new instance of this class, based on the <code>user.name</code> system property.
   */
  public static AuditInfo forCurrentUser() {
    String username = System.getProperty("user.name");
    Assertions.illegalState(username == null, "user.name system property not defined");
    return forUser(username);
  }
  
  /**
   * @param ctx an {@link EncryptionContext}, providing the {@link PublicKey} and {@link Cipher} to use.
   * @return a new instance of this class, consisting of an encrypted copy of this instance.
   */
  public AuditInfo encryptWith(EncryptionContext ctx) {
    return encryptWith(ctx.getPublicKey(), ctx.getCipher());
  }
  
  /**
   * @param key the {@link PrivateKey} to use to perform encryption.
   * @param cipher the {@link Cipher} to use to perform encryption.
   * @return a new instance of this class, consisting of an encrypted copy of this instance.
   */
  public AuditInfo encryptWith(PublicKey key, Cipher cipher) {
    if (encryptedToken == null) {
      try {
        byte[] encrypted = cipher.doFinal(userToken.getBytes());
        Assertions.illegalState(encrypted.length == 0, "Encrypted payload size is 0");
        return new AuditInfo(encrypted, type);
      } catch (Exception e) {
        throw new IllegalStateException("Error performing encryption", e);
      }
    }
    return this;
  }
  
  /**
   * @param ctx the {@link DecryptionContext} to use.
   * @return a new instance of this class, consisting of a decrypted copy of this instance.
   */
  public AuditInfo decryptWith(DecryptionContext ctx) {
    return decryptWith(ctx.getPrivateKey(), ctx.getCipher());
  }
  
  /**
   * @param key the {@link PrivateKey} to use to perform decryption.
   * @param cipher the {@link Cipher} to use to perform decryption.
   * @param toDecrypt the bytes corresponding to the encrypted data to decrypt.
   * @return a new instance of this class, consisting of a decrypted copy of this instance.
   */
  public AuditInfo decryptWith(PrivateKey key, Cipher cipher) {
    if (userToken == null) {
      try {
        byte[] decrypted = cipher.doFinal(encryptedToken);
        userToken = new String(decrypted);
        return new AuditInfo(userToken, type);
      } catch (Exception e) {
        throw new IllegalStateException("Error performing encryption", e);
      }
    }
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
    userToken = (String) in.readObject();
    type = (TokenType) in.readObject();
    encryptedToken = (byte[]) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(userToken);
    out.writeObject(type);
    out.writeObject(encryptedToken);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AuditInfo) {
      AuditInfo other = (AuditInfo) obj;
      if (userToken != null) {
        return ObjectUtils.safeEquals(userToken, other.userToken)
        &&  ObjectUtils.safeEquals(type, other.type);
      } else {
        return ObjectUtils.safeEquals(encryptedToken, other.encryptedToken)
        &&  ObjectUtils.safeEquals(type, other.type);
      }
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return userToken.hashCode();
  }

}
