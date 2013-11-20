package org.sapia.corus.client.facade;

import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;

/**
 * Provides the interface for managing shell scripts kept in Corus.
 * 
 * @author yduchesne
 * 
 */
public interface ShellScriptManagementFacade {

  /**
   * @param criteria
   *          the criteria to use to select shell scripts to remove.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue removeScripts(ShellScriptCriteria criteria, ClusterInfo cluster);

  /**
   * Executes the shell script corresponding to the given alias.
   * 
   * @param scriptAlias
   *          the alias of the script to execute.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return the {@link ProgressQueue} holding progress data.
   * @throws IOException
   *           if the script's execution could not be invoked.
   * @throws ScriptNotFoundException
   *           if no script could be found for the given alias.
   */
  public ProgressQueue execScript(String scriptAlias, ClusterInfo cluster) throws IOException, ScriptNotFoundException;

  /**
   * @param criteria
   *          the {@link ShellScriptCriteria} to use.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return the {@link List} of {@link ShellScript}s matching the given
   *         criteria.
   */
  public Results<List<ShellScript>> getScripts(ShellScriptCriteria criteria, ClusterInfo cluster);
}
