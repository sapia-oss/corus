package org.sapia.corus.admin;

import org.sapia.corus.exceptions.CorusException;


/**
 * This class is the corus server's remote interface.
 *
 * @author Yanick Duchesne
 */
public interface Corus extends java.rmi.Remote {

  
  /**
   * @return the version of the corus server.
   */
  public String getVersion();
  
  /**
   * @return the domain of the Corus server.
   */
  public String getDomain();
  
  /**
   * @param moduleName the name of the module to lookup.
   * @return the remote module instance.
   * @throws CorusException in an error occurs while performing
   * the lookup.
   */
  public Object lookup(String moduleName) throws CorusException;
  
}
