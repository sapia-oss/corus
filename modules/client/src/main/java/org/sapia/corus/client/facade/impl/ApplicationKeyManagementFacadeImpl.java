package org.sapia.corus.client.facade.impl;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.facade.ApplicationKeyManagementFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.CorusSecurityException;

/**
 * Implements the {@link ApplicationKeyManagementFacade} in front of the {@link ApplicationKeyManager} module.
 * 
 * @author yduchesne
 *
 */
public class ApplicationKeyManagementFacadeImpl extends FacadeHelper<ApplicationKeyManager> implements ApplicationKeyManagementFacade {
  
  public ApplicationKeyManagementFacadeImpl(CorusConnectionContext context) {
    super(context, ApplicationKeyManager.class);
  }
  
  @Override
  public void changeApplicationKey(String appId, String appKey,
      ClusterInfo cluster) throws IllegalArgumentException {
    proxy.changeApplicationKey(appId, appKey);
    invoker.invokeLenient(void.class, cluster);        
  }
  
  @Override
  public void changeRole(String appId, String role, ClusterInfo cluster)
      throws IllegalArgumentException, CorusSecurityException {
    proxy.changeRole(appId, role);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void createApplicationKey(String appId, String appKey, String role, ClusterInfo cluster) 
      throws CorusSecurityException {
    proxy.addOrUpdateApplicationKey(appId, appKey, role);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void removeAppKey(ArgMatcher appId, ClusterInfo cluster) {
    proxy.removeAppKey(appId);
    invoker.invokeLenient(void.class, cluster); 
  }
  
  @Override
  public Results<List<AppKeyConfig>> getAppKeyInfos(ArgMatcher appId, ClusterInfo cluster) {
    Results<List<AppKeyConfig>> results = new Results<>();
    proxy.getAppKeyConfig(appId);
    invoker.invokeLenient(results, cluster);
    return results;
  }

}
