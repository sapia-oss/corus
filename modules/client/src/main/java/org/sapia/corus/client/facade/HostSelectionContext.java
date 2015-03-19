package org.sapia.corus.client.facade;

import java.util.Collection;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * Describes stack behavior for host selection, which allows CLI command pushing hosts
 * being selected for the next clustered command.
 * 
 * @author yduchesne
 *
 */
public interface HostSelectionContext {

  /**
   * @return the {@link Collection} of hosts from the top of the selection stack - and removes that collection from the stack.
   */
  public OptionalValue<Collection<CorusHost>> pop();
  
  /**
   * @return the {@link Collection} of hosts from the top of the selection stack.
   */
  public OptionalValue<Collection<CorusHost>> peek();
  
  /**
   * @param targets the {@link Collection} of hosts to push onto the selection stack.
   */
  public void push(Collection<CorusHost> targets);
    
}
