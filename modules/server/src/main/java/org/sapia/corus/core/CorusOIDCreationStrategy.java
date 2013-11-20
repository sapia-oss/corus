package org.sapia.corus.core;

import org.sapia.corus.client.Corus;
import org.sapia.corus.client.transport.CorusOID;
import org.sapia.ubik.rmi.server.oid.DefaultOIDCreationStrategy;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.oid.OIDCreationStrategy;

/**
 * A {@link OIDCreationStrategy} used to create {@link CorusOID}s.
 * 
 * @author yduchesne
 * 
 */
public class CorusOIDCreationStrategy extends DefaultOIDCreationStrategy {

  @Override
  public boolean apply(Object toExport) {
    return toExport instanceof Corus;
  }

  @Override
  public OID createOID(Object toExport) {
    return new CorusOID();
  }
}
