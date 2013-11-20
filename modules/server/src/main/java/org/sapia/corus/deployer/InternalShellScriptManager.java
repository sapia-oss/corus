package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;

/**
 * Internal extension to the {@link ShellScriptManager} interface.
 * 
 * @author yduchesne
 * 
 */
public interface InternalShellScriptManager extends ShellScriptManager {

  /**
   * Adds the given shell script to this instance. Deletes the existing one (for
   * the given shell script's alias) if any.
   * 
   * @param script
   *          the {@link ShellScript} to add.
   * @param file
   *          the script's actual {@link File}.
   */
  public ProgressQueue addScript(ShellScript script, File file);

  /**
   * @param script
   *          a {@link ShellScript} instance.
   * @return the {@link File} corresponding to the given shell script.
   * @throws FileNotFoundException
   *           if no such file is found.
   */
  public File getScriptFile(ShellScript script) throws FileNotFoundException;

}
