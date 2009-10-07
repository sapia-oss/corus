package org.sapia.corus.admin.services.naming;

import javax.naming.Context;

import org.sapia.corus.admin.Module;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;


/**
 * Specifies a naming service based on JNDI.
 * 
 * @author Yanick Duchesne
 */
public interface JndiModule extends java.rmi.Remote, Module, RemoteContextProvider {
  public static final String ROLE = JndiModule.class.getName();

  /**
   * @return the JNDI root {@link Context}.
   */
  public javax.naming.Context getContext();
}
