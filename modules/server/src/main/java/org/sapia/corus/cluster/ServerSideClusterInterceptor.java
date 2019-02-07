package org.sapia.corus.cluster;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.log.Logger;

import org.sapia.corus.client.common.encryption.DecryptionContext;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.CorusCallback;
import org.sapia.corus.client.services.cluster.CorusCallbackCapable;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.TimeValue;

/**
 * Handles clustered commands on the server-side.
 * 
 * @author yduchesnes
 */
public class ServerSideClusterInterceptor implements CorusCallback {

  private Logger                  log;
  private ServerContext           context;
  private ClusterManager          cluster;
  private Func<Connections, ServerAddress> connectionPoolSupplier;
  private boolean                 lenient;
  private Consumer<CorusHost>     invalidNodeListener;
  private ExecutorService         outboundIoExecutor;
  private TimeValue               outboundCommandTimeout = new TimeValue(30, TimeUnit.SECONDS);
  
  /**
   * @param log the {@link Logger} to use.
   * @param context the {@link ServerContext}.
   * @param connectionPoolSupplier a function used to obtain a {@link Connections} instance.
   * @param invalidNodeListener a {@link Consumer} to notify when connecting to a remote node fails.
   * @param outboundIoExecutor the {@link ExecutorService} to use for sending commands over the wire.
   * @param lenient if <code>true</code>, indicates that clustered commands execution errors due to network failures
   *                should be ignored.
   */
  ServerSideClusterInterceptor(
          Logger log, 
          ServerContext context, 
          Func<Connections, ServerAddress> connectionPoolSupplier, 
          Consumer<CorusHost> invalidNodeListener,
          ExecutorService outboundIoExecutor,
          boolean lenient) {
    this.log                    = log;
    this.context                = context;
    this.cluster                = context.getServices().lookup(ClusterManager.class);
    this.connectionPoolSupplier = connectionPoolSupplier;
    this.invalidNodeListener    = invalidNodeListener;
    this.outboundIoExecutor     = outboundIoExecutor;
    this.lenient                = lenient;
  }

  /**
   * @param log the {@link Logger} to use.
   * @param context the {@link ServerContext}.
   * @param invalidNodeListener a {@link Consumer} to notify when connecting to a remote node fails.
   * @param outboundIoExecutor the {@link ExecutorService} to use for sending commands over the wire.
   * @param lenient if <code>true</code>, indicates that clustered commands execution errors due to network failures
   *                should be ignored.
   */
  ServerSideClusterInterceptor(
      Logger log, 
      ServerContext context, 
      Consumer<CorusHost> invalidNodeListener, 
      ExecutorService outboundIoExecutor,
      boolean lenient) {
    this(log, context, new Func<Connections, ServerAddress>() {
      @Override
      public Connections call(ServerAddress nextTarget) {
        try {
          return Hub.getModules().getTransportManager().getConnectionsFor(nextTarget);
        } catch (RemoteException e)  {
          throw new IllegalStateException("Network error occurred while performing operation", e);
        }
      }
    }, invalidNodeListener, outboundIoExecutor, lenient);
  }
  
  /**
   * 
   * @param outboundCommandTimeout
   */
  public void setOutboundCommandTimeout(TimeValue outboundCommandTimeout) {
    this.outboundCommandTimeout = outboundCommandTimeout;
  }

  @Override
  public boolean isLenient() {
    return lenient;
  }

  /**
   * @param evt an {@link IncomingCommandEvent} instance.
   */
  public void onIncomingCommandEvent(IncomingCommandEvent evt) {
    if (evt.getCommand() instanceof CorusCallbackCapable) {
      CorusCallbackCapable capable = (CorusCallbackCapable) evt.getCommand();
      capable.setCorusCallback(this);
    }
  }

  // --------------------------------------------------------------------------
  // CorusCallback interface

  public org.sapia.corus.client.Corus getCorus() {
    return context.getCorus();
  }

  @Override
  public void debug(String message) {
    log.debug(message);
  }

  @Override
  public boolean isDebug() {
    return log.isDebugEnabled();
  }

  @Override
  public void error(String message, Throwable err) {
    log.error(message, err);
  }

  @Override
  public Set<ServerAddress> getSiblings() {
    return cluster.getHostAddresses();
  }
  
  @Override
  public DecryptionContext getDecryptionContext() {
    return Encryption.getDefaultDecryptionContext(context.getKeyPair().getPrivate());
  }

  @Override
  public Object send(ClusteredCommand cmd, ServerAddress nextTarget) throws Exception {
    Future<Object> result = outboundIoExecutor.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return doSend(cmd,  nextTarget);
      }
    });
    return result.get(outboundCommandTimeout.getValue(), outboundCommandTimeout.getUnit());
  }
  
  private Object doSend(ClusteredCommand cmd, ServerAddress nextTarget) throws Exception {
    Connections pool = connectionPoolSupplier.call(nextTarget);
    AuditInfo currentAuditInfo = null;

    CorusHost nextHost = cluster.resolveHost(nextTarget);
    if (cmd.getAuditInfo().isSet()) {
      Assertions.illegalState(cmd.getAuditInfo().get().isEncrypted(), "Expected AuditInfo to have been decrypted at this point");
      log.debug("AuditInfo is set: encrypting with public key of next targeted host");
      currentAuditInfo = cmd.getAuditInfo().get();
      cmd.setAuditInfo(cmd.getAuditInfo().get().encryptWith(Encryption.getDefaultEncryptionContext(nextHost.getPublicKey())));
    }
    
    RmiConnection conn = null;
    try {
      conn = pool.acquire();
      conn.send(cmd);
      Object returnValue = conn.receive();
      pool.release(conn);
      // ensuring that we're not going to set the AuditInfo back to "decrypted"
      currentAuditInfo = null;
      return returnValue;
    } catch (RemoteException re) {
      if (conn != null) {
        pool.invalidate(conn);
      }
      pool.clear();
      invalidNodeListener.accept(nextHost);
      throw re;
    } catch (Exception e) {
      log.error("Problem sending clustered command to " + nextTarget, e);
      if (conn != null) {
        conn.close();
      }
      throw e;
    } finally {
      // If the following is true, an error occurred (see above reset of currentAuditInfo variable).
      // In this case, we want to set the AuditInfo back to decrypted so that a "send" can be retried.
      if (currentAuditInfo != null) {
        cmd.resetAuditInfo(currentAuditInfo);
      }
    }

  }

}
