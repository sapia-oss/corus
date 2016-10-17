package org.sapia.corus.client.services.cluster;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
public class NonClusteredCommandTest {

  @Mock
  private CorusCallback callback;
  
  @Mock
  private Corus corus;
  
  @Mock
  private ServerAddress clientAddress, address, address2;
  
  @Mock
  private Connection conn;
  
  @Mock
  private Auditor auditor;
  
  private TestRemoteObject remoteObject;
  
  private CorusHost host;

  private KeyPair keypair;
  
  private EncryptionContext encryption;
  
  private DecryptionContext decryption;
  
  private NonClusteredCommand cmd;
  
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
    cmd = new NonClusteredCommand(new TestCommand());
    cmd.init(new Config(address, conn));
  }

  @Test
  public void testSetAuditInfo() {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testSetAuditInfo_unencrypted() {
    cmd.setAuditInfo(AuditInfo.forUser("test"));
  }

  @Test(expected = IllegalStateException.class)
  public void testExecute_callback_not_set() throws Throwable {
    cmd.execute();
  }

  @Test
  public void testExecute() throws Throwable {
    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(encryption));
    cmd.setCorusCallback(callback);
    cmd.execute();
    
    verify(auditor).audit(any(AuditInfo.class), any(ServerAddress.class), anyString(), anyString());
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
