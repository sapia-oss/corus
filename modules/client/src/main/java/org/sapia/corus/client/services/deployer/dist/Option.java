package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdElement;


/**
 * This class corresponds to the <code>option</code> element in the
 * corus.xml file. This element represents a standard "java" option,
 * such as "-cp ...", etc.
 *
 * @author Yanick Duchesne
 */
public class Option extends Property implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;

  /**
   * @see org.sapia.corus.client.services.deployer.dist.Param#convert()
   */
  public CmdElement convert() {
    return new org.sapia.console.Option(_name, _value);
  }

  public String toString() {
    return "[ name=" + getName() + ", value=" + getValue() + " ]";
  }
}
