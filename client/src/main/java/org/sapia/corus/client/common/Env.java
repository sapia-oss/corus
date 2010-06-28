package org.sapia.corus.client.common;

import org.sapia.corus.client.services.deployer.dist.Property;

/**
 * This interface specifies methods allowing to retrieve specific environment 
 * properties of a Corus server instance. An instance of this interface is passed
 * in from within the Corus server.
 * 
 * @author yduchesne
 *
 */
public interface Env {

  /**
   * @return the distribution directory of the process to start.
   */
  public String getDistDir();

  /**
   * @return the "common" directory of the process to start.
   */
  public String getCommonDir();

  /**
   * @return the process directory of the process to start.
   */
  public String getProcessDir();
  
  /**
   * @return the name of the profile under which to start the process.
   */
  public String getProfile();

  /**
   * @return the properties to pass to the started process.
   */
  public Property[] getProperties();
  
  /**
   * @param basedir the base directory that the returned path filter should scan from.
   * @return a new {@link PathFilter}
   */
  public PathFilter createPathFilter(String basedir);
}
