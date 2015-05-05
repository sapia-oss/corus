package org.sapia.corus.cloud.topology;

import java.util.HashSet;

import java.util.Set;

/**
 * Base class whose instances are meant to hold {@link Param}s.
 * 
 * @author yduchesne
 *
 */
public abstract class ParamContainer {

  private Set<Param> params = new HashSet<Param>();
  
  /**
   * @param p a {@link Param} to add to this instance.
   */
  public void addParam(Param p) {
    params.add(p);
  }
  
  /**
   * @return the {@link Set} of {@link Param}s held by this instance.
   */
  public Set<Param> getParams() {
    return params;
  }
  
  /**
   * @param others a {@link Set} of other {@link Param}s to add to this instance.
   */
  public void addParams(Set<Param> others) {
    params.addAll(params);
  }
}
