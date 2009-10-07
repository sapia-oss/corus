package org.sapia.corus.util;

/**
 * Specifies the behavior of configuration properties.
 * 
 * @author yduchesne
 *
 */
public interface Property {

  public String getValue();
  
  public int getIntValue();
  
  public long getLongValue();
  
  public boolean getBooleanValue();
}

