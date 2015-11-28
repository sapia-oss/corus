package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.*;

import java.security.KeyPair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo.AuditInfoRegistration;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class CurrentAuditInfoTest {
  
  @Mock
  private ServerAddress address, address2;
  
  private CorusHost host, host2;
 

  @Before
  public void setUp() throws Exception {
    CurrentAuditInfo.unset();
    
    KeyPair kp = Encryption.generateDefaultKeyPair();
    host = CorusHost.newInstance(
        new Endpoint(address, address), 
        "osInfo", 
        "jvmInfo", 
        kp.getPublic()
    );
    
    host2 = CorusHost.newInstance(
        new Endpoint(address2, address2), 
        "osInfo", 
        "jvmInfo", 
        kp.getPublic()
    );
  }
  
  @After
  public void tearDown() {
    CurrentAuditInfo.unset();
  }

  @Test
  public void testIsSet() {
    CurrentAuditInfo.set(AuditInfo.forUser("test"), host);
    assertTrue(CurrentAuditInfo.isSet());
  }
  
  @Test
  public void testIsSet_false() {
    assertFalse(CurrentAuditInfo.isSet());
  }


  @Test
  public void testIsNull() {
    assertTrue(CurrentAuditInfo.isNull());
  }
  
  @Test
  public void testIsNull_false() {
    CurrentAuditInfo.set(AuditInfo.forUser("test"), host);
    assertFalse(CurrentAuditInfo.isNull());
  }

  @Test
  public void testGet() {
    CurrentAuditInfo.set(AuditInfo.forUser("test"), host);
    AuditInfoRegistration reg = CurrentAuditInfo.get().get();
    assertEquals("test", reg.getAuditInfo().getUserToken());
    assertEquals(host, reg.getHost());
  }

  @Test
  public void testSetAuditInfoCorusHost() {
    CurrentAuditInfo.set(AuditInfo.forUser("test"), host);
    AuditInfoRegistration reg = CurrentAuditInfo.get().get();
    assertEquals("test", reg.getAuditInfo().getUserToken());
    assertEquals(host, reg.getHost());
  }

  @Test
  public void testSetAuditInfoRegistrationCorusHost() {
    CurrentAuditInfo.set(AuditInfo.forUser("test"), host);
    AuditInfoRegistration reg = CurrentAuditInfo.get().get();
    
    CurrentAuditInfo.set(reg, host2);
   
    AuditInfoRegistration reg2 = CurrentAuditInfo.get().get();
    assertEquals("test", reg2.getAuditInfo().getUserToken());
    assertEquals(host2, reg2.getHost());
  }

}
