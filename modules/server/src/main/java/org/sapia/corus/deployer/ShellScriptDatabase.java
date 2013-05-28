package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;

/**
 * Specifies {@link ShellScript} persistence operations.
 * 
 * @author yduchesne
 *
 */
public interface ShellScriptDatabase {

  /**
   * @param script a {@link ShellScript} to add.
   */
  public void addScript(ShellScript script);
  
  /**
   * @param alias the alias of the shell script to remove.
   */
  public void removeScript(String alias);
  
  /**
   * @param criteria the {@link ShellScriptCriteria} to use.
   * @return the {@link List} of {@link ShellScript}s matching the given criteria, and which
   * have been deleted from this instance.
   */
  public List<ShellScript> removeScript(ShellScriptCriteria criteria);
  
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
   * @param alias the script's alias.
   * @return the {@link ShellScript} corresponding to the given alias.
   */
  public ShellScript getScript(String alias) throws ScriptNotFoundException;
}
