package org.sapia.corus.client.services.cluster;

import java.util.Set;


import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.replication.ReplicatedCommand;
import org.sapia.ubik.rmi.server.command.InvokeCommand;


/**
 * @author Yanick Duchesne
 */
public class ClusteredCommand extends ReplicatedCommand {
  /* Do not call; used for externalization only */
  public ClusteredCommand() {
  }

  public ClusteredCommand(InvokeCommand cmd, ClusteredInvoker invoker, Set<ServerAddress> targets) {
  	super(cmd, targets, invoker, false);
  }
  
	public Object execute() throws Throwable {
		return super.execute();
	}  
  
  
  
  

}
