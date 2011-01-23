package org.sapia.corus.client.services.security;

import java.rmi.Remote;

import org.sapia.corus.client.Module;

/**
 * @author Yanick Duchesne
 */
public interface SecurityModule extends Module{

  /** Defines the role name of this module. */  
  public static final String ROLE = SecurityModule.class.getName();

}
