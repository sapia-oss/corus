package org.sapia.corus.core;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.transport.CorusModuleOID;
import org.sapia.ubik.rmi.server.oid.DefaultOIDCreationStrategy;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.oid.OIDCreationStrategy;

/**
 * A {@link OIDCreationStrategy} used to create {@link Module} {@link OID}s.
 * 
 * @author yduchesne
 *
 */
public class CorusModuleOIDCreationStrategy extends DefaultOIDCreationStrategy {
  
  @Override
  public boolean apply(Object toExport) {
    return toExport instanceof Module;
  }

  @Override
  public OID createOID(Object toExport) {
    Module mod = (Module) toExport;
    return new CorusModuleOID(mod.getRoleName());
  }
}
