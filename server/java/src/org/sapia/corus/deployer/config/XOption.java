package org.sapia.corus.deployer.config;

import org.sapia.console.CmdElement;


/**
 * This class corresponds to the <code>x-option</code> element in the
 * corus.xml file. This element pertains to the special "X" options that
 * can be specified at the "java" command-line.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class XOption extends Option implements java.io.Serializable {
  
  /**
   * @see org.sapia.corus.deployer.config.Param#convert()
   */
  public CmdElement convert() {
    return new org.sapia.console.Option("X" + _name + _value);
  }
}
