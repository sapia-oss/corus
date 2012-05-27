package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdElement;
import org.sapia.ubik.util.Strings;


/**
 * This class corresponds to the <code>xoption</code> element in the
 * corus.xml file. This element pertains to the special "X" options that
 * can be specified at the "java" command-line.
 *
 * @author Yanick Duchesne
 */
public class XOption extends Option implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;

  /**
   * @see org.sapia.corus.client.services.deployer.dist.Param#convert()
   */
  public CmdElement convert() {
    if (Strings.isBlank(name)) {
      return new org.sapia.console.Arg("X" + value);
    } else {
      return new org.sapia.console.Option("X" + name + value);
    }
  }
}
