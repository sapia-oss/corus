package org.sapia.corus.client.services.cluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.rmi.RemoteException;
import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.common.encryption.DecryptionContext;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.common.encryption.EncryptionContext;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.transport.CorusModuleOID;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ClusteredCommandTest {

  @Mock
  private CorusCallback callback;
  
  @Mock
  private Corus corus;
  
  @Mock
  private ServerAddress clientAddress, address, address2, address3;
  
  @Mock
  private Connection conn;
  
  @Mock
  private Auditor auditor;
  
  private TestRemoteObject remoteObject;
  
  private CorusHost host;

  private KeyPair keypair;
  
  private EncryptionContext encryption;
  
  private DecryptionContext decryption;
  
  private ClusteredCommand cmd;
   
  @Before
  public void setUp() throws Exception {
    keypair = Encryption.generateDefaultKeyPair();
    encryption = Encryption.getDefaultEncryptionContext(keypair.getPublic());
    decryption = Encryption.getDefaultDecryptionContext(keypair.getPrivate());
    host = CorusHost.newInstance(
        "test-node", 
        new Endpoint(address, address), 
        "osInfo", 
        "jvmInfo", 
        keypair.getPublic()
    );

    remoteObject = new TestRemoteObject();
    
    when(conn.getServerAddress()).thenReturn(clientAddress);
    when(corus.getHostInfo()).thenReturn(host);
    when(corus.lookup(Auditor.ROLE)).thenReturn(auditor);
    when(corus.lookup("test-remote-object")).thenReturn(remoteObject);
    when(callback.getCorus()).thenReturn(corus);
    when(callback.getDecryptionContext()).thenReturn(decryption);
    when(callback.getSiblings()).thenReturn(Collects.arrayToSet(address2));
    cmd = new ClusteredCommand(new TestCommand());
    cmd.init(new Config(address, conn));
  }
  
  @Test
  public void testSetAuditInfo_encrypted() {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testSetAuditInfo_decrypted() {
    cmd.setAuditInfo(AuditInfo.forUser("test"));
  }
  
  public void testResetAuditInfo_decrypted() {
    cmd.setAuditInfo(AuditInfo.forUser("test"));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testResetAuditInfo_encrypted() {
	cmd.resetAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
  }

  @Test
  public void testExecute_all_hosts() throws Throwable {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    
    assertEquals(new Integer(0), cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getVisited().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test
  public void testExecute_all_hosts_with_network_failure_and_lenient_enabled() throws Throwable {
    when(callback.isLenient()).thenReturn(true);
    setUpForNetworkFailure();
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);

    assertEquals(new Integer(0), cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getVisited().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test
  public void testExecute_all_hosts_with_network_failure_and_lenient_disabled() throws Throwable {
    when(callback.isLenient()).thenReturn(true);
    setUpForNetworkFailure();
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);

    assertEquals(new Integer(0), cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getVisited().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test
  public void testExecute_specific_hosts() throws Throwable {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address, address2));
    
    assertEquals(new Integer(0), cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getTargeted().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test
  public void testExecute_specific_hosts_with_network_failure_and_lenient_enabled() throws Throwable {
    setUpForNetworkFailure();
    when(callback.isLenient()).thenReturn(true);

    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address, address2, address3));

    assertEquals(new Integer(0), cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getTargeted().contains(address2));
    assertTrue(cmd.getTargeted().contains(address3));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback, times(2)).send(any(), any());
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test(expected = RemoteException.class)
  public void testExecute_specific_hosts_with_network_failure_and_lenient_disabled() throws Throwable {
    setUpForNetworkFailure();

    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address, address2, address3));

    cmd.execute();
  }
  
  @Test
  public void testExecute_specific_hosts_not_current_host() throws Throwable {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address2));
    
    assertNull(cmd.execute());
    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getTargeted().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor, never()).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test
  public void testExecute_specific_hosts_not_current_host_with_network_failure_and_lenient_enabled() throws Throwable {
    setUpForNetworkFailure();
    when(callback.isLenient()).thenReturn(true);
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address2, address3));

    cmd.execute();

    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getTargeted().contains(address2));
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback).send(cmd, address2);
    verify(auditor, never()).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());

  }

  @Test(expected = RemoteException.class)
  public void testExecute_specific_hosts_not_current_host_with_network_failure_and_lenient_disabled() throws Throwable {
    setUpForNetworkFailure();
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.addTargets(Collects.arrayToSet(address2, address3));
    cmd.execute();
  }
  
  @Test
  public void testExecute_exclude_host() throws Throwable {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.exclude(Collects.arrayToSet(address2));

    cmd.execute();

    assertTrue(cmd.getVisited().contains(address));
    assertTrue(cmd.getVisited().contains(address2));;
    assertFalse(cmd.getAuditInfo().get().isEncrypted());
    verify(callback, never()).send(cmd, address2);
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
  }

  @Test(expected = IllegalStateException.class)
  public void testExecute_corus_callback_not_set() throws Throwable {
    cmd.execute();
  }

  @Test
  public void testSelectNextAddress() {
  }

  private void setUpForNetworkFailure() {
    try {
      doAnswer(new Answer<Object>() {

        private int invocationCount;

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          if (invocationCount == 0) {
            throw new RemoteException("Network Error");
          }
          invocationCount++;

          return "test-result";
        }
      }).when(callback).send(any(), any());
    } catch (Throwable e) {
      throw new IllegalStateException("Error setting up mock callback for network failure", e);
    }
  }

  public static class TestCommand extends InvokeCommand {
    
    public TestCommand() {
      super(new CorusModuleOID("test-remote-object"),  "invoke", new Object[]{}, new Class<?>[]{}, null);
    }
   
    @Override
    public Object execute() throws Throwable {
      return null;
    }
    
  }
  
  public static class TestRemoteObject {
    
    public Integer invoke() {
      return 0;
    }
    
  }

}
