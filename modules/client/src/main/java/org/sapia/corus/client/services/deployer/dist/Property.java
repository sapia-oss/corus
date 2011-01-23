package org.sapia.corus.client.services.deployer.dist;

import org.sapia.console.CmdElement;
import org.sapia.console.Option;


/**
 * This class corresponds to the <code>property</code> element in the
 * corus.xml file. THe "property" element corresponds to a VM property,
 * usually specified through "-D" options at the command-line.
 *
 * @author Yanick Duchesne
 */
public class Property implements Param, java.io.Serializable {

  static final long serialVersionUID = 1L;

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
   * @see org.sapia.corus.client.services.deployer.dist.Param#convert()
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
