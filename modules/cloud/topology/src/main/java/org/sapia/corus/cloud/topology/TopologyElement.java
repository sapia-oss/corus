package org.sapia.corus.cloud.topology;

/**
 * Implemented by non-template topology elements that may inherit from templates.
 * 
 * @author yduchesne
 *
 */
public interface TopologyElement {
  
  /**
   * @param context the {@link TopologyContext}.
   */
  public void render(TopologyContext context);

}
