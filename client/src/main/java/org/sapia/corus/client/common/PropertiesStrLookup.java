package org.sapia.corus.client.common;

import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;

/**
 * This class implements lookup behavior over a {@link Properties} instance.
 * 
 * @author yduchesne
 *
 */
public class PropertiesStrLookup extends StrLookup{

  Properties props;
  
  public PropertiesStrLookup(Properties props) {
    this.props = props;
  }
  
  @Override
  public String lookup(String name) {
    return props.getProperty(name);
  }
  
  /**
   * @return an instance of this class constructed using {@link System#getProperties()}
   */
  public static PropertiesStrLookup getSystemInstance(){
    return new PropertiesStrLookup(System.getProperties());
  }

}
