package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.ClusterFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusterStatus;

public class ClusterfacadeImpl extends FacadeHelper<ClusterManager> implements ClusterFacade {

  public ClusterfacadeImpl(CorusConnectionContext context) {
    super(context, ClusterManager.class);
  }

  @Override
  public Results<ClusterStatus> getClusterStatus(ClusterInfo cluster) {
    Results<ClusterStatus> results = new Results<ClusterStatus>();
    proxy.getClusterStatus();
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public void resync() {
    proxy.resync();
    invoker.invokeLenient(void.class, new ClusterInfo(false));
  }
  
  @Override
  public void changeCluster(String newClusterName, ClusterInfo cluster) {
    proxy.changeCluster(newClusterName.trim());
    try {
      invoker.invoke(void.class, cluster);
    } catch (Throwable e) {
      throw new IllegalStateException("Error occurred trying to change cluster", e);
    }
  }

}
