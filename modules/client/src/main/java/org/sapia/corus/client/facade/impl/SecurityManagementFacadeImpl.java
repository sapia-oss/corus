package org.sapia.corus.client.facade.impl;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.SecurityManagementFacade;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;

/**
 * Implements the {@link SecurityManagementFacade} interface.
 * 
 * @author yduchesne
 *
 */
public class SecurityManagementFacadeImpl extends FacadeHelper<SecurityModule> implements SecurityManagementFacade {

  public SecurityManagementFacadeImpl(CorusConnectionContext context) {
    super(context, SecurityModule.class);
  }
  
  @Override
  public void addRole(String role, Set<Permission> permissions, ClusterInfo cluster) 
      throws IllegalArgumentException {
    proxy.addRole(role, permissions);
    invoker.invokeLenient(void.class, cluster);    
  }
  
  @Override
  public void addOrUpdateRole(String role, Set<Permission> permissions,
      ClusterInfo cluster) {
    proxy.addOrUpdateRole(role, permissions);
    invoker.invokeLenient(void.class, cluster);        
  }
  
  @Override
  public Results<List<RoleConfig>> getRoleConfig(Arg role, ClusterInfo cluster) {
    Results<List<RoleConfig>> results = new Results<>();
    proxy.getRoleConfig(role);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public void removeRole(String role, ClusterInfo cluster) 
      throws IllegalArgumentException {
    proxy.removeRole(role);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void udpateRole(String role, Set<Permission> permissions, ClusterInfo cluster) 
      throws IllegalArgumentException {
    proxy.updateRole(role, permissions);
    invoker.invokeLenient(void.class, cluster);
  }
  
}
