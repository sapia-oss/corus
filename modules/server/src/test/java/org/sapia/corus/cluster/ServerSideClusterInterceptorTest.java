package org.sapia.corus.cluster;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.security.KeyPair;

import org.apache.log.Hierarchy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.transport.CorusModuleOID;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class ServerSideClusterInterceptorTest {

  @Mock
  private ServerContext  context;
  
  @Mock
  private ClusterManager cluster;
  
  @Mock
  private Connections    connections;
  
  @Mock
  private RmiConnection  rmiConnection;
  
  @Mock
  private ServerAddress  nextAddress;
  
  @Mock
  private Func<Connections, ServerAddress> connectionSupplier;
  
  private InternalServiceContext services;
 
  private ClusteredCommand cmd;
  
  private ServerSideClusterInterceptor interceptor;
  
  @Before
  public void setUp() throws Exception {
    services    = new InternalServiceContext();
    services.bind(ClusterManager.class, cluster);
    cmd         = new ClusteredCommand(new TestCommand());
    KeyPair kp = Encryption.generateDefaultKeyPair();
    CorusHost host = CorusHost.newInstance(
        "test-node", 
        new Endpoint(nextAddress, nextAddress), 
        "osInfo", 
        "jvmInfo", 
        kp.getPublic()
    );
    
    when(connectionSupplier.call(any(ServerAddress.class))).thenReturn(connections);
    when(connections.acquire()).thenReturn(rmiConnection);
    when(context.getServices()).thenReturn(services);
    when(context.getKeyPair()).thenReturn(kp);
    when(cluster.resolveHost(any(ServerAddress.class))).thenReturn(host);

    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(Encryption.getDefaultEncryptionContext(context.getKeyPair().getPublic())));
    interceptor = new ServerSideClusterInterceptor(Hierarchy.getDefaultHierarchy().getRootLogger(), context, connectionSupplier);
  }

  @Test
  public void testSend() throws Exception {
    IncomingCommandEvent evt = new IncomingCommandEvent(cmd);
    interceptor.onIncomingCommandEvent(evt);
    cmd.decrypt(interceptor.getDecryptionContext());
    interceptor.send(cmd, nextAddress);
    
    assertTrue("Expected AuditInfo to have been encrypted prior to cascading command to next host", cmd.getAuditInfo().get().isEncrypted());
  }
  
  
  @Test(expected = IllegalStateException.class)
  public void testSend_already_encrypted_audit_info() throws Exception {
    IncomingCommandEvent evt = new IncomingCommandEvent(cmd);
    cmd.setAuditInfo(cmd.getAuditInfo().get().decryptWith(interceptor.getDecryptionContext()));
    interceptor.onIncomingCommandEvent(evt);
    interceptor.send(cmd, nextAddress);
    
  }
  
  public static class TestCommand extends InvokeCommand {
    
    public TestCommand() {
      super(new CorusModuleOID("test"),  "testMethod", new Object[]{}, new Class<?>[]{}, null);
    }
   
    @Override
    public Object execute() throws Throwable {
      return null;
    }
    
  }

}
