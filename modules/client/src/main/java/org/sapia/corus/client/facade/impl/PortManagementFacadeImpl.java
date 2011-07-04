package org.sapia.corus.client.facade.impl;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.PortManagementFacade;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;

public class PortManagementFacadeImpl extends FacadeHelper<PortManager> implements PortManagementFacade {
  
  public PortManagementFacadeImpl(CorusConnectionContext context) {
    super(context, PortManager.class);
  }
  
  @Override
  public void addPortRange(String name, int min, int max, ClusterInfo cluster)
      throws PortRangeConflictException, PortRangeInvalidException {
    proxy.addPortRange(name, min, max);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void addPortRanges(List<PortRange> ranges, boolean clearExisting, ClusterInfo cluster)
      throws PortRangeConflictException, PortRangeInvalidException {
    proxy.addPortRanges(ranges, clearExisting);
    invoker.invokeLenient(void.class, cluster);    
  }
  
  @Override
  public Results<List<PortRange>> getPortRanges(ClusterInfo cluster) {
    Results<List<PortRange>> results = new Results<List<PortRange>>();
    proxy.getPortRanges();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public void releasePortRange(String rangeName, ClusterInfo cluster) {
    proxy.releasePortRange(ArgFactory.parse(rangeName));
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void removePortRange(String name, boolean force, ClusterInfo cluster)
      throws PortActiveException {
    proxy.removePortRange(ArgFactory.parse(name), force);
    invoker.invokeLenient(void.class, cluster);
  }
 
}
