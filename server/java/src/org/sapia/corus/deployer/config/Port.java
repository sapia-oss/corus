/*
 * Port.java
 *
 * Created on October 18, 2005, 10:39 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.deployer.config;

import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 *
 * @author yduchesne
 */
public class Port implements java.io.Serializable, ObjectCreationCallback{
  
  private String _name;
  
  /** Creates a new instance of Port */
  public Port() {
  }
  
  public void setName(String name){
    _name = name;
  }
  
  public String getName(){
    return _name;
  }
  
  public String toString(){
    return new StringBuffer("[").append(_name).append("]").toString();
  }
  
  public Object onCreate() throws ConfigurationException{
    if(_name == null){
      throw new ConfigurationException("Port name not set");
    }
    return this;
  }  
}
