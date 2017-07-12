package org.sapia.corus.repository;

import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

/**
 * Holds constants corresponding to the different types of 
 * messages/events/notifications exchanged in the context of repo server/client
 * coordination.
 */
public enum RepoEventType {

  ARTIFACT_LIST_REQUEST(RepoRole.CLIENT, RepoRole.SERVER),
  DISTRIBUTION_DEPLOYMENT_REQUEST(RepoRole.CLIENT, RepoRole.SERVER),
  FILE_DEPLOYMENT_REQUEST(RepoRole.CLIENT, RepoRole.SERVER),
  SHELL_SCRIPT_DEPLOYMENT_REQUEST(RepoRole.CLIENT, RepoRole.SERVER),
  CONFIG_DEPLOYMENT_REQUEST(RepoRole.CLIENT, RepoRole.SERVER),
  
  DISTRIBUTION_LIST_RESPONSE(RepoRole.SERVER, RepoRole.CLIENT),
  FILE_LIST_RESPONSE(RepoRole.SERVER, RepoRole.CLIENT),
  SHELL_SCRIPT_LIST_RESPONSE(RepoRole.SERVER, RepoRole.CLIENT),
  
  EXEC_CONFIG_NOTIFICATION(RepoRole.SERVER, RepoRole.CLIENT),
  PORT_RANGE_NOTIFICATION(RepoRole.SERVER, RepoRole.CLIENT),
  SECURITY_CONFIG_NOTIFICATION(RepoRole.SERVER, RepoRole.CLIENT),
  CONFIG_NOTIFICATION(RepoRole.SERVER, RepoRole.CLIENT);
  
  private RepoRole sourceType, destType;
  
  private RepoEventType(RepoRole sourceType, RepoRole destType) {
    this.sourceType = sourceType;
    this.destType   = destType;
  }
  
  
  public RepoRole sourceType() {
    return sourceType;
  }
  
  public RepoRole destType() {
    return destType;
  }
}
