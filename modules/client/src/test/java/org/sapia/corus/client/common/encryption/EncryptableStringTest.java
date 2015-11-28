package org.sapia.corus.client.common.encryption;

import static org.junit.Assert.*;

import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class EncryptableStringTest {

  private KeyPair kp;
  
  @Before
  public void setUp() throws Exception {
    kp = Encryption.generateDefaultKeyPair();
  }

  @Test
  public void testGetValue_decrypted() {
    assertEquals("test", EncryptableString.of("test").getValue().get());
  }
  
  @Test
  public void testGetValue_encrypted() {
    Encryptable<String> es = EncryptableString.of("test").encrypt(Encryption.getDefaultEncryptionContext(kp.getPublic()));
    assertTrue(es.getValue().isNull());
  }

  @Test
  public void testIsDecrypted() {
    assertTrue(EncryptableString.of("test").isDecrypted());
    assertFalse(EncryptableString.of("test").isEncrypted());
  }

  @Test
  public void testIsEncrypted() {
    Encryptable<String> es = EncryptableString.of("test").encrypt(Encryption.getDefaultEncryptionContext(kp.getPublic()));
    assertTrue(es.isEncrypted());
    assertFalse(es.isDecrypted());
  }

  @Test
  public void testDecrypt() {
    Encryptable<String> encrypted = EncryptableString.of("test").encrypt(Encryption.getDefaultEncryptionContext(kp.getPublic()));
    Encryptable<String> decrypted = encrypted.decrypt(Encryption.getDefaultDecryptionContext(kp.getPrivate()));
    assertEquals("test", decrypted.getValue().get());
  }
  
  @Test
  public void testSerialization_encrypted() throws Exception {
    Encryptable<String> encrypted = EncryptableString.of("test").encrypt(Encryption.getDefaultEncryptionContext(kp.getPublic()));
    byte[] payload = Serialization.serialize(encrypted);
    Encryptable<String> copy = (Encryptable<String>) Serialization.deserialize(payload);
    assertEquals(encrypted, copy);
  }

  @Test
  public void testSerialization_decrypted() throws Exception {
    Encryptable<String> encrypted = EncryptableString.of("test");
    byte[] payload = Serialization.serialize(encrypted);
    Encryptable<String> copy = (Encryptable<String>) Serialization.deserialize(payload);
    assertEquals(encrypted, copy);
  }
}
