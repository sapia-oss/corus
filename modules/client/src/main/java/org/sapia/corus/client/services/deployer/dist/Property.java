package org.sapia.corus.client.services.deployer.dist;

import java.util.Objects;

import org.sapia.console.CmdElement;
import org.sapia.console.Option;

/**
 * This class corresponds to the <code>property</code> element in the corus.xml
 * file. THe "property" element corresponds to a VM property, usually specified
 * through "-D" options at the command-line.
 * 
 * @author Yanick Duchesne
 */
public class Property implements Param, java.io.Serializable {

  static final long serialVersionUID = 1L;

  protected String name;
  protected String value;

  public Property() {
  }

  public Property(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setValue(String val) {
    value = val;
  }

  public String getValue() {
    return value;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.dist.Param#convert()
   */
  public CmdElement convert() {
    if (value.indexOf(" ") == -1) {
      return new Option("D" + name + "=" + value);
    } else {
      return new Option("D" + name + "=\"" + value + "\"");
    }
  }

  @Override
  public int hashCode() {
      return Objects.hash(name, value);
  }

  @Override
  public boolean equals(Object other) {
      if (!(other instanceof Property)) {
          return false;
      }
      
      return Objects.equals(name, ((Property) other).name)
              && Objects.equals(value, ((Property) other).value);
  }
  
  public String toString() {
    return "[ name=" + name + ", value=" + value + " ]";
  }
}
