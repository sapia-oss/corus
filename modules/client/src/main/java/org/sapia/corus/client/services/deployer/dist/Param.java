package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdElement;

/**
 * Specifies the behavior of converting to a command-line element.
 * 
 * @author Yanick Duchesne
 */
public interface Param {

  public CmdElement convert();
}
