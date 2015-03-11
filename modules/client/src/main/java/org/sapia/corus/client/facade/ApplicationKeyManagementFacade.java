package org.sapia.corus.client.facade;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.CorusSecurityException;

/**
 * A facade for the {@link ApplicationKeyManager}.
 * 
 * @author yduchesne
 *
 */
public interface ApplicationKeyManagementFacade {
  
  /**
   * @param appId an {@link ArgMatcher} instance corresponding to one or more application keys to remove.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   */
  public void removeAppKey(ArgMatcher appId, ClusterInfo cluster);
  
  /**
   * @param appId an application ID for which to create an application key.
   * @param appKey the application key to create.
   * @param role the name of the role to associate to the application key.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @throws CorusSecurityException if the role specified is invalid.
   */
  public void createApplicationKey(String appId, String appKey, String role, ClusterInfo cluster) 
      throws CorusSecurityException;

  /**
   * @param appId an {@link ArgMatcher} instance corresponding to one or more application key information
   * to remove.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @return a {@link List}s of {@link AppKeyConfig}s (one for each targeted hosts).
   */
  public Results<List<AppKeyConfig>> getAppKeyInfos(ArgMatcher appId, ClusterInfo cluster);
  
  /**
   * Changes the application key associated to an existing application ID.
   * 
   * @param appId the application ID for which to generate a new application key.
   * @param appKey a new application key.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @return the new application key that was generated.
   * @throws IllegalArgumentException if no such application ID exists.
   */
  public void changeApplicationKey(String appId, String appKey, ClusterInfo cluster) 
      throws IllegalArgumentException;
  
  /**
   * Changes the role associated to an existing application ID.
   * 
   * @param appId the application ID to which to associate a new role.
   * @param role the role to associate to the given application ID.
   * @param cluster a {@link ClusterInfo} indicating if this operation should be clustered.
   * @throws IllegalArgumentException if no such application ID exists.
   * @throws CorusSecurityException if the role specified is invalid.
   */
  public void changeRole(String appId, String role, ClusterInfo cluster) 
      throws IllegalArgumentException, CorusSecurityException;
  
}
