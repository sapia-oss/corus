package org.sapia.corus.client.services.cluster;

import java.lang.reflect.Method;
import java.util.Set;

import org.sapia.corus.client.Corus;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.replication.ReplicatedInvoker;

/**
 * @author Yanick Duchesne
 */
public class ClusteredInvoker implements ReplicatedInvoker{
	
  static final long serialVersionUID = 1L;
  
	private String                   _moduleName;
	private transient ClusterManager _cluster;
	private transient Corus          _corus;

  public ClusteredInvoker() {}
  
  /**
   * @see org.sapia.ubik.rmi.replication.ReplicatedInvoker#invoke(java.lang.String, java.lang.Class[], java.lang.Object[])
   */
  public Object invoke(String methodName, 
                       Class<?>[] types, 
                       Object[] args)
                       throws Throwable {
		Object toReturn = null;

		try {
			Object module = _corus.lookup(_moduleName);
			Method toCall = module.getClass().getMethod(methodName,
																									types);
/*
			if (_log.isInfoEnabled()) {
				_log.info("Processing clustered command: " + methodName);
			}
*/
			toCall.invoke(module, args);
		} catch (Throwable t) {
			//_log.error("Error processing cluster event", t);

			throw t;
		}
		/*

		if ((toReturn != null) && toReturn instanceof ProgressQueue) {
			try {
				ProgressQueueLogger.transferMessages(_log, (ProgressQueue) toReturn);
			} catch (Throwable t) {
				_log.error("Error processing cluster event", t);
			}
			
		} */ 
		return toReturn;                     	
  }
  
  public void setModuleName(String moduleName){
		_moduleName = moduleName;  	
  }
  
  public void setUp(Corus corus, ClusterManager cluster) {
    _corus      = corus;
  	_cluster    = cluster;
  }
  

  public Set<ServerAddress> getSiblings() {
    return _cluster.getHostAddresses();
  }

  
}
