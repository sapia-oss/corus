package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNull;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.sapia.console.CmdElement;
import org.sapia.console.Option;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * This class corresponds to the <code>property</code> element in the corus.xml
 * file. THe "property" element corresponds to a VM property, usually specified
 * through "-D" options at the command-line.
 * 
 * @author Yanick Duchesne
 */
public class Property implements Param, java.io.Serializable, ObjectCreationCallback {

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
  
  @Override
  public CmdElement convert() {
    if (value.indexOf(" ") == -1 || StringUtils.isBlank(value)) {
      return new Option("D" + name + "=" + value);
    } else if (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"') {
      // Assume the value was properly enclosed into double quotes 
      return new Option("D" + name + "=" + value);
    } else {
      return new Option("D" + name + "=\"" + value + "\"");
    }
  }
  
  @Override
  public Object onCreate() throws ConfigurationException {
    doValidate("property");
    return this;
  }
  
  protected void doValidate(String elementName) throws ConfigurationException {
    attributeNotNullOrEmpty(elementName, "name", name);
    attributeNotNull(elementName, "value", value);    
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
