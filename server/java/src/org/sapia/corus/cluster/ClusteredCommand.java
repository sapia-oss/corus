package org.sapia.corus.cluster;

import java.util.Set;

import org.sapia.ubik.rmi.replication.ReplicatedCommand;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClusteredCommand extends ReplicatedCommand {
  /* Do not call; used for externalization only */
  public ClusteredCommand() {
  }

  ClusteredCommand(InvokeCommand cmd, ClusteredInvoker invoker, Set targets) {
  	super(cmd, targets, invoker, false);
  }
  
	public Object execute() throws Throwable {
		return super.execute();
	}  
  
  
  
  

}
