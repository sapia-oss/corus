package org.sapia.corus.deployer;

import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;


public class DeployerStateHandler {
  
  public Reference<ModuleState> stateReference = new DefaultReference<ModuleState>(ModuleState.IDLE);


}
