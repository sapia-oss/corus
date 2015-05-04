package org.sapia.corus.client;

import org.sapia.corus.client.exceptions.core.ServiceNotFoundException;
import org.sapia.corus.client.services.cluster.CorusHost;

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
   * 
   * @return the {@link CorusHost} instance which holds information about the
   *         Corus server.
   */
  public CorusHost getHostInfo();

  /**
   * @param moduleName
   *          the name of the module to lookup.
   * @return the remote module instance.
   * @throws ServiceNotFoundException
   *           when the desired service is not found.
   */
  public Object lookup(String moduleName) throws ServiceNotFoundException;

  /**
   * @return a JSON dump of this instance's internal state.
   */
  public String dump();


}
