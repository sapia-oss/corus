package org.sapia.corus.client.facade.impl;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.PortManagementFacade;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;

public class PortManagementFacadeImpl extends FacadeHelper<PortManager> implements PortManagementFacade {

  public PortManagementFacadeImpl(CorusConnectionContext context) {
    super(context, PortManager.class);
  }

  @Override
  public void addPortRange(String name, int min, int max, ClusterInfo cluster) throws PortRangeConflictException, PortRangeInvalidException {
    proxy.addPortRange(name, min, max);
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void addPortRanges(List<PortRange> ranges, boolean clearExisting, ClusterInfo cluster) throws PortRangeConflictException,
      PortRangeInvalidException {
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
  public void removePortRange(String name, boolean force, ClusterInfo cluster) throws PortActiveException {
    proxy.removePortRange(ArgMatchers.parse(name), force);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void archive(RevId revId, ClusterInfo cluster) {
    proxy.archive(revId);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void unarchive(RevId revId, ClusterInfo cluster) {
    proxy.unarchive(revId);
    invoker.invokeLenient(void.class, cluster);
  }
}
