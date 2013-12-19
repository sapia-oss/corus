package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;

/**
 * Interface common to implementations that implement parts of a command-line.
 * 
 * @author yduchesne
 *
 */
public interface CmdGenerator {

  /**
   * @param env the {@link Env} instance to use.
   * @param toAppendTo the {@link CmdLine} to append to.
   */
  public void generate(Env env, CmdLine toAppendTo);
}
