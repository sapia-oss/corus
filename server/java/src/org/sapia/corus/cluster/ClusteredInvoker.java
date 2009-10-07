package org.sapia.corus.cluster;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.log.Logger;
import org.sapia.corus.CorusRuntime;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueLogger;
import org.sapia.ubik.rmi.replication.ReplicatedInvoker;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClusteredInvoker implements ReplicatedInvoker{
	
	private String                   _moduleName;
	private transient Logger         _log;	
	private transient ClusterManager _cluster;

  public ClusteredInvoker() {}
  
  /**
   * @see org.sapia.ubik.rmi.replication.ReplicatedInvoker#invoke(java.lang.String, java.lang.Class[], java.lang.Object[])
   */
  public Object invoke(String methodName, 
                       Class[] types, 
                       Object[] args)
                       throws Throwable {
		Object toReturn = null;

		try {
			Object module = CorusRuntime.getCorus().lookup(_moduleName);
			Method toCall = module.getClass().getMethod(methodName,
																									types);

			if (_log.isInfoEnabled()) {
				_log.info("Processing clustered command: " + methodName);
			}

			toCall.invoke(module, args);
		} catch (Throwable t) {
			_log.error("Error processing cluster event", t);

			throw t;
		}

		if ((toReturn != null) && toReturn instanceof ProgressQueue) {
			try {
				ProgressQueueLogger.transferMessages(_log, (ProgressQueue) toReturn);
			} catch (Throwable t) {
				_log.error("Error processing cluster event", t);
			}
			
		}  
		return toReturn;                     	
  }
  
  void setModuleName(String moduleName){
		_moduleName = moduleName;  	
  }
  
  void setUp(ClusterManager cluster, Logger log){
  	_log        = log;
  	_cluster    = cluster;
  }
  
  /**
   * @see org.sapia.ubik.rmi.replication.ReplicatedInvoker#getSiblings()
   */
  public Set getSiblings() {
    return ClusterManagerImpl.instance.getHostAddresses();
  }



}
