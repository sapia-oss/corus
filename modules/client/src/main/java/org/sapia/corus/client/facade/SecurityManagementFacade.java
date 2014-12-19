package org.sapia.corus.client.facade;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;

/**
 * This interface acts as a facade to the {@link SecurityModule}.
 * 
 * @author yduchesne
 *
 */
public interface SecurityManagementFacade {

  /**
   * @param role an {@link Arg} instance specifying for which role(s) to return the associated data.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @return a {@link Results} containing a {@link List} of {@link RoleConfig} instances.
   */
  public Results<List<RoleConfig>> getRoleConfig(Arg role, ClusterInfo cluster);

  /**
   * Creates a new role, associating the given permissions to it.
   * 
   * @param role a role.
   * @param permissions a {@link Set} of {@link Permission}s.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @throws IllegalArgumentException if the given role already exists.
   */
  public void addRole(String role, Set<Permission> permissions, ClusterInfo cluster) throws IllegalArgumentException;
  
  /**
   * Creates a new role, associating the given permissions to it.
   * 
   * @param role a role.
   * @param permissions a {@link Set} of {@link Permission}s.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   */
  public void addOrUpdateRole(String role, Set<Permission> permissions, ClusterInfo cluster);
  
  /**
   * Changes the permissions associated to the given role.
   * 
   * @param role a role.
   * @param permissions a {@link Set} of {@link Permission}s.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @throws IllegalArgumentException if the given role is not found.
   */
  public void udpateRole(String role, Set<Permission> permissions, ClusterInfo cluster) throws IllegalArgumentException;

  /**
   * Removes a role and its associated permissions from the system.
   * 
   * @param role a role.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @throws IllegalArgumentException if the given role is not found.
   */
  public void removeRole(String role, ClusterInfo cluster) throws IllegalArgumentException;
}
