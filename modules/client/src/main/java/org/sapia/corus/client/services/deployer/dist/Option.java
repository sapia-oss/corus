package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdElement;
import org.sapia.ubik.util.Strings;

/**
 * This class corresponds to the <code>option</code> element in the corus.xml
 * file. This element represents a standard "java" option, such as "-cp ...",
 * etc.
 * 
 * @author Yanick Duchesne
 */
public class Option extends Property implements java.io.Serializable {

  static final long serialVersionUID = 1L;

  /**
   * @see org.sapia.corus.client.services.deployer.dist.Param#convert()
   */
  public CmdElement convert() {
    if (Strings.isBlank(name)) {
      return new org.sapia.console.Arg(value);
    } else {
      return new org.sapia.console.Option(name, value);
    }
  }

  public String toString() {
    return "[ name=" + getName() + ", value=" + getValue() + " ]";
  }
}
