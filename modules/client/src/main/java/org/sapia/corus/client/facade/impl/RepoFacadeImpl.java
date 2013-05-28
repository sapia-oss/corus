package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.RepoFacade;
import org.sapia.corus.client.services.repository.Repository;

/**
 * {@link RepoFacade} implementation.
 * 
 * @author yduchesne
 *
 */
public class RepoFacadeImpl extends FacadeHelper<Repository> implements RepoFacade {
  
  public RepoFacadeImpl(CorusConnectionContext context) {
    super(context, Repository.class);
  }  

  @Override
  public void pull(ClusterInfo cluster) {
    proxy.pull();
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void push(ClusterInfo cluster) {
    proxy.push();
    invoker.invokeLenient(void.class, cluster);
  }
}
