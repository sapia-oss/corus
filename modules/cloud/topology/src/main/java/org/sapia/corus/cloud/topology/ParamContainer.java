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
  
  /**
   * @param paramName the name of a parameter.
   * @return the {@link Param} instance corresponding to the given name.
   * @throws IllegalArgumentException if no such parameter could be found.
   */
  public Param getParam(String paramName) throws IllegalArgumentException {
    for (Param p : params) {
      if (p.getName().equals(paramName)) {
        return p;
      }
    }
    throw new IllegalArgumentException("No parameter found for: " + paramName);
  }
  
  /**
   * @param paramName the name of a parameter.
   * @param defaultVal the default value to use, if no param is found for the given name.
   * @return the {@link Param} instance corresponding to the given name.
   */
  public Param getParam(String paramName, String defaultVal) {
    for (Param p : params) {
      if (p.getName().equals(paramName)) {
        return p;
      }
    }
    return Param.of(paramName, defaultVal);
  }
  
  /**
   * @param paramName a parameter name.
   * @return <code>true</code> if this instance holds a {@link Param} instance with the given name.
   */
  public boolean existsParam(String paramName) {
    for (Param p : params) {
      if (p.getName().equals(paramName)) {
        return true;
      }
    }
    return false;
  }
}
