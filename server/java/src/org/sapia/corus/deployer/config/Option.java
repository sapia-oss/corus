package org.sapia.corus.deployer.config;

import org.sapia.console.CmdElement;


/**
 * This class corresponds to the <code>option</code> element in the
 * corus.xml file. This element represents a standard "java" option,
 * such as "-cp ...", etc.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Option extends Property implements java.io.Serializable {
  
  /**
   * @see org.sapia.corus.deployer.config.Param#convert()
   */
  public CmdElement convert() {
    return new org.sapia.console.Option(_name, _value);
  }

  public String toString() {
    return "[ name=" + getName() + ", value=" + getValue() + " ]";
  }
}
