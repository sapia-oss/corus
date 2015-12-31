package org.sapia.corus.deployer;

import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.ubik.rmi.interceptor.Interceptor;


public class DeployerStateHandler implements Interceptor {
  
  public Reference<ModuleState> stateReference = new DefaultReference<ModuleState>(ModuleState.IDLE);


}
