package org.sapia.corus.client.common;

import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.corus.client.services.deployer.dist.Property;

/**
 * This class implements lookup behavior over a {@link Properties} instance.
 * 
 * @author yduchesne
 * 
 */
public class PropertiesStrLookup extends StrLookup {

  Properties props;

  public PropertiesStrLookup(Properties props) {
    this.props = props;
  }

  @Override
  public String lookup(String name) {
    return props.getProperty(name);
  }

  /**
   * @return an instance of this class constructed using
   *         {@link System#getProperties()}
   */
  public static PropertiesStrLookup getSystemInstance() {
    return new PropertiesStrLookup(System.getProperties());
  }
  
  /**
   * @param props
   *          the {@link Properties} to use.
   * @return a new instance of this class.
   */
  public static PropertiesStrLookup getInstance(Properties props) {
    return new PropertiesStrLookup(props);
  }

  /**
   * @param propsArray
   *          the array of {@link Property} to use.
   * @return a new instance of this class.
   */
  public static PropertiesStrLookup getInstance(Property[] propsArray) {
    Properties props = new Properties();
    for (Property p : propsArray) {
      if (p.getName() != null && p.getValue() != null) {
        props.setProperty(p.getName(), p.getValue());
      }
    }
    return new PropertiesStrLookup(props);
  }
}
