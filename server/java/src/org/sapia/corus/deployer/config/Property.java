package org.sapia.corus.deployer.config;

import org.sapia.console.CmdElement;
import org.sapia.console.Option;


/**
 * This class corresponds to the <code>property</code> element in the
 * corus.xml file. THe "property" element corresponds to a VM property,
 * usually specified through "-D" options at the command-line.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Property implements Param, java.io.Serializable {
  protected String _name;
  protected String _value;

  public Property() {
  }

  public Property(String name, String value) {
    _name  = name;
    _value = value;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setValue(String val) {
    _value = val;
  }

  public String getValue() {
    return _value;
  }

  /**
   * @see org.sapia.corus.deployer.config.Param#convert()
   */
  public CmdElement convert() {
    if (_value.indexOf(" ") == -1) {
      return new Option("D" + _name + "=" + _value);
    } else {
      return new Option("D" + _name + "=\"" + _value + "\"");
    }
  }

  public String toString() {
    return "[ name=" + _name + ", value=" + _value + " ]";
  }
}
