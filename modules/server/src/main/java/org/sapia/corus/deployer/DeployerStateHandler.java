package org.sapia.corus.deployer;

import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.deployer.event.DeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStartingEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentUnzippedEvent;
import org.sapia.ubik.rmi.interceptor.Interceptor;


public class DeployerStateHandler implements Interceptor {
  
  public Reference<ModuleState> stateReference = new DefaultReference<ModuleState>(ModuleState.IDLE);


}
