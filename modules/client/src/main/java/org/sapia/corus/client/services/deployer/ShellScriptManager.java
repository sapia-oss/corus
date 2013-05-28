package org.sapia.corus.client.services.deployer;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ProgressQueue;

/**
 * Specifies the interface of the module managing shell scripts. 
 * 
 * @author yduchesne
 *
 */
public interface ShellScriptManager extends Remote, Module {
  
  String ROLE = ShellScriptManager.class.getName();
  
  /**
   * @param criteria the {@link ShellScriptCriteria} to use.
   * @param a {@link ProgressQueue}.
   */
  public ProgressQueue removeScripts(ShellScriptCriteria criteria);
  
  /**
   * @return all the {@link ShellScript}s that this instance holds.
   */
  public List<ShellScript> getScripts();
  
  /**
   * @param criteria the {@link ShellScriptCriteria} to use.
   * @return the {@link List} of {@link ShellScript}s matching the given criteria.
   */
  public List<ShellScript> getScripts(ShellScriptCriteria criteria);
  
  /**
   * @param alias the alias of the script to execute.
   * @throws ScriptNotFoundException if no such script is found.
   * @throws IOException if an error occurs trying to invoke the script.
   */
  public ProgressQueue executeScript(String alias) throws ScriptNotFoundException, IOException;

}
