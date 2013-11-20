package org.sapia.corus.client.facade.impl;

import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.ShellScriptManagementFacade;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.client.services.deployer.ShellScriptManager;

/**
 * Implements the {@link ShellScriptManagementFacade} interface.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptManagementFacadeImpl extends FacadeHelper<ShellScriptManager> implements ShellScriptManagementFacade {

  public ShellScriptManagementFacadeImpl(CorusConnectionContext context) {
    super(context, ShellScriptManager.class);
  }

  @Override
  public synchronized Results<List<ShellScript>> getScripts(ShellScriptCriteria criteria, ClusterInfo cluster) {
    Results<List<ShellScript>> results = new Results<List<ShellScript>>();
    proxy.getScripts(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public synchronized ProgressQueue removeScripts(ShellScriptCriteria criteria, ClusterInfo cluster) {
    proxy.removeScripts(criteria);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

  @Override
  public ProgressQueue execScript(String scriptAlias, ClusterInfo cluster) throws IOException, ScriptNotFoundException {
    proxy.executeScript(scriptAlias);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

}
