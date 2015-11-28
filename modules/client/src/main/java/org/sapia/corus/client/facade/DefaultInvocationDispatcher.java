package org.sapia.corus.client.facade;

import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo;

/**
 * The default {@link InvocationDispatcher}, which wraps a
 * {@link CorusConnectionContextImpl}. The invocations are sent over the wire to
 * the appropriate Corus instances.
 * 
 * @author yduchesne
 * 
 */
public class DefaultInvocationDispatcher implements InvocationDispatcher {

  private CorusConnectionContext context;

  public DefaultInvocationDispatcher(CorusConnectionContext context) {
    this.context = context;
  }

  @Override
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    boolean auditInfoWasSet = false;
    try {
      if (CurrentAuditInfo.isNull()) {
        CurrentAuditInfo.set(AuditInfo.forCurrentUser(), context.getServerHost());
        auditInfoWasSet = true;
      } 
      return context.invoke(returnType, moduleInterface, method, params, info);
    } catch (NoSuchObjectException | org.sapia.ubik.rmi.NoSuchObjectException e) {
      context.reconnect();
      return context.invoke(returnType, moduleInterface, method, params, info);
    } finally {
      if(auditInfoWasSet) {
        CurrentAuditInfo.unset();
      }
    }
  }

  @Override
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
    boolean auditInfoWasSet = false;
    try {
      if (CurrentAuditInfo.isNull()) {
        CurrentAuditInfo.set(AuditInfo.forCurrentUser(), context.getServerHost());
        auditInfoWasSet = true;
      } 
      context.invoke(results, moduleInterface, method, params, cluster);
    } catch (NoSuchObjectException | org.sapia.ubik.rmi.NoSuchObjectException e) {
      context.reconnect();
      context.invoke(results, moduleInterface, method, params, cluster);
    } finally {
      if (auditInfoWasSet) {
        CurrentAuditInfo.unset();
      }
    }
  }

}
