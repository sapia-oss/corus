package org.sapia.corus.client.facade;

import java.util.Collection;
import java.util.Stack;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * Defaults implementation of the {@link HostSelectionContext} interface.
 * 
 * @author yduchesne
 *
 */
public class HostSelectionContextImpl implements HostSelectionContext {
  
  private Stack<Collection<CorusHost>> hosts = new Stack<Collection<CorusHost>>();
  
  @Override
  public OptionalValue<Collection<CorusHost>> pop() {
    if (hosts.isEmpty()) {
      return new OptionalValue<Collection<CorusHost>>(null);
    }
    return new OptionalValue<Collection<CorusHost>>(hosts.pop());
  }
  
  @Override
  public OptionalValue<Collection<CorusHost>> peek() {
    return new OptionalValue<Collection<CorusHost>>(hosts.peek());
  }
  
  @Override
  public void push(Collection<CorusHost> targets) {
    hosts.push(targets);
  }

}
