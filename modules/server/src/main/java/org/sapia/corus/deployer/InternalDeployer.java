package org.sapia.corus.deployer;

import java.io.File;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;

/**
 * The internal deployer interface, providing methods that are accessible from within the Corus server only.
 * 
 * @author yduchesne
 *
 */
public interface InternalDeployer extends Deployer {

  /**
   * @param name a distribution name.
   * @param version a distribution version.
   * @return the archive {@link File} corresponding to the given distribution name and version.
   * @throws DistributionNotFoundException if no such distribution exists.
   */
  public File getDistributionFile(String name, String version) throws DistributionNotFoundException;
}
