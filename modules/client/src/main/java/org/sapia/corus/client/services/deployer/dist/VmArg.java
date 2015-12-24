package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;

import org.sapia.console.Arg;
import org.sapia.console.CmdElement;
import org.sapia.ubik.util.Strings;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Corresponds to a VM argument.
 * 
 * @author yduchesne
 * 
 */
public class VmArg implements Param, Serializable, ObjectCreationCallback {

  public static final String ELEMENT_NAME = "arg";
  
  static final long serialVersionUID = 1L;

  private String value;

  /**
   * @param value
   *          a value.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return this instance's value.
   */
  public String getValue() {
    return value;
  }

  @Override
  public CmdElement convert() {
    if (Strings.isBlank(value)) {
      return new Arg("");
    } else {
      return new Arg(value);
    }
  }
  
  @Override
  public Object onCreate() throws ConfigurationException {
    ConfigAssertions.attributeNotNull("arg", "value", value);
    return this;
  }
}
