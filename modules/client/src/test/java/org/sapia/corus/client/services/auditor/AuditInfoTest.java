package org.sapia.corus.client.services.auditor;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.ubik.util.Serialization;

public class AuditInfoTest {

  private AuditInfo info;
  
  @Before
  public void setUp() throws Exception {
    info = AuditInfo.forClientId("test-test-test-test-test-test-test-test-test");
  }

  @Test
  public void testEncryption() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();

    Cipher cp = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cp.init(Cipher.ENCRYPT_MODE, kp.getPublic());
    AuditInfo encrypted = info.encryptWith(kp.getPublic(), cp);
    
    cp = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cp.init(Cipher.DECRYPT_MODE, kp.getPrivate());
    AuditInfo decrypted = encrypted.decryptWith(kp.getPrivate(), cp);

    assertEquals(info, decrypted);
    
    assertEquals(info.getRequestId(), decrypted.getRequestId());
  }
  
  @Test
  public void testEncryption_with_default_encryption_config() throws Exception {
    KeyPair kp = Encryption.generateDefaultKeyPair();
    AuditInfo encrypted = info.encryptWith(Encryption.getDefaultEncryptionContext(kp.getPublic()));
    AuditInfo decrypted = encrypted.decryptWith(Encryption.getDefaultDecryptionContext(kp.getPrivate()));
    assertEquals(info, decrypted);
    assertEquals(info.getRequestId(), decrypted.getRequestId());
  }

  @Test
  public void testSerialization() throws Exception {
    byte[] serialized = Serialization.serialize(info);
    AuditInfo copy = (AuditInfo) Serialization.deserialize(serialized);
    assertEquals(info, copy);
    assertEquals(info.getRequestId(), copy.getRequestId());
  }
}
