package org.sapia.corus.core;

public interface PropertyProvider {
  
  public void overrideInitProperties(PropertyContainer properties);
  
  public PropertyContainer getInitProperties();
}
