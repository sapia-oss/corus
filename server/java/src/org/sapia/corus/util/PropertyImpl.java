package org.sapia.corus.util;

import java.io.Serializable;

import org.sapia.corus.InitContext;

/**
 * This class models a configuration property (name and value).
 * 
 * @author yduchesne
 *
 */
public class PropertyImpl implements Property, Serializable{
  
  static final long serialVersionUID = 1L;
  
  private String value;
  
  public void setName(String name) {
    this.value = InitContext.get().getProperties().getProperty(name);
  }

  public String getValue(){
    checkNull();
    return value;
  }
  
  public int getIntValue(){
    checkNull();
    return Integer.parseInt(value);
  }
  
  public long getLongValue(){
    checkNull();
    return Long.parseLong(value);
  }

  public boolean getBooleanValue(){
    checkNull();
    return Boolean.parseBoolean(value);
  }
  
  @Override
  public String toString() {
    return getValue();
  }
  
  private void checkNull(){
    if(value == null){
      throw new IllegalStateException("No value specified for property " + value);
    }
  }
  
}
