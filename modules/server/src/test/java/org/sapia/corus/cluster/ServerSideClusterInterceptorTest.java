package org.sapia.corus.cluster;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


import java.rmi.RemoteException;
import java.security.KeyPair;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.log.Hierarchy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
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
  
  @Mock 
  private Consumer<CorusHost> invalidHostListener;
  
  @Mock
  private ExecutorService outboundCommandPool;
  
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
    
    doAnswer(new Answer<Future<Object>>() {
      @Override
      public Future<Object> answer(InvocationOnMock invocation) throws Throwable {
        Callable<Object> task = invocation.getArgumentAt(0, Callable.class);
        return new TestFuture<Object>(task.call());
      }
    }).when(outboundCommandPool).submit(any(Callable.class));

    cmd.setAuditInfo(AuditInfo.forUser("test").encryptWith(Encryption.getDefaultEncryptionContext(context.getKeyPair().getPublic())));
    interceptor = new ServerSideClusterInterceptor(Hierarchy.getDefaultHierarchy().getRootLogger(), 
        context, connectionSupplier, invalidHostListener, outboundCommandPool, false);
  }

  @Test
  public void testSend() throws Exception {
    IncomingCommandEvent evt = new IncomingCommandEvent(cmd);
    interceptor.onIncomingCommandEvent(evt);
    cmd.decrypt(interceptor.getDecryptionContext());
    interceptor.send(cmd, nextAddress);
    
    assertTrue("Expected AuditInfo to have been encrypted prior to cascading command to next host", cmd.getAuditInfo().get().isEncrypted());
  }


  @Test
  public void testSend_decrypted_after_send_failure() throws Exception {
    doThrow(new RemoteException("ERROR")).when(rmiConnection).send(any());

    IncomingCommandEvent evt = new IncomingCommandEvent(cmd);
    interceptor.onIncomingCommandEvent(evt);
    cmd.decrypt(interceptor.getDecryptionContext());
    try {
      interceptor.send(cmd, nextAddress);
    } catch (RemoteException e) {
      // noop
    }
    assertFalse("Expected AuditInfo to have been switched back to decrypted after error on send", cmd.getAuditInfo().get().isEncrypted());
  }

  @Test
  public void testSend_invalid_host_listener_was_notified() throws Exception {
    doThrow(new RemoteException("ERROR")).when(rmiConnection).send(any());

    IncomingCommandEvent evt = new IncomingCommandEvent(cmd);
    interceptor.onIncomingCommandEvent(evt);
    cmd.decrypt(interceptor.getDecryptionContext());
    try {
      interceptor.send(cmd, nextAddress);
    } catch (RemoteException e) {
      // noop
    }
    invalidHostListener.accept(any());
  }
  
  @Test(expected = IllegalStateException.class)
  public void testSend_already_encrypted_audit_info() throws Exception {
    doThrow(new RemoteException("ERROR")).when(rmiConnection).send(any());
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

  private static class TestFuture<T> implements Future<T> {
   
    private T toReturn;
    
    private TestFuture(T toReturn) {
      this.toReturn = toReturn;
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
      return toReturn;
    }
    
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return get();
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return true;
    }
    
    @Override
    public boolean isCancelled() {
      return false;
    }
    
    @Override
    public boolean isDone() {
      return false;
    }
    
    
  }
}
