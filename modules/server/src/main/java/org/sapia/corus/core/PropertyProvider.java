package org.sapia.corus.core;

/**
 * Abstracts the behavior for acquiring initialization properties.
 * 
 * @author yduchesne
 *
 */
public interface PropertyProvider {
  
	/**
	 * Internally overrides this instance's initialization properties with the 
	 * given properties.
	 * 
	 * @param properties a {@link PropertyContainer}.
	 */
  public void overrideInitProperties(PropertyContainer properties);
  
  /**
   * @return a {@link PropertyContainer} corresponding to this instance's initialization properties.
   */
  public PropertyContainer getInitProperties();
}
