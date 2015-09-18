/*
 * Port.java
 *
 * Created on October 18, 2005, 10:39 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.client.services.deployer.dist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;
import org.sapia.util.xml.confix.ObjectHandlerIF;

/**
 * 
 * @author yduchesne
 */
public class Port implements Externalizable, ObjectCreationCallback, ObjectHandlerIF {

  static final long serialVersionUID = 1L;

  private String _name;
  private OptionalValue<? extends DiagnosticConfig> _diagnosticConfig = OptionalValue.none();

  /** Creates a new instance of Port */
  public Port() {
  }

  public void setName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
  
  public OptionalValue<? extends DiagnosticConfig> getDiagnosticConfig() {
    return _diagnosticConfig;
  }
  
  // --------------------------------------------------------------------------
  // Configuration unmarshalling
  
  public Object onCreate() throws ConfigurationException {
    if (_name == null) {
      throw new ConfigurationException("Port name not set");
    }
    return this;
  }
  
  @Override
  public void handleObject(String name, Object toHandle)
      throws ConfigurationException {
    if (toHandle instanceof DiagnosticConfig) {
      _diagnosticConfig = OptionalValue.of((DiagnosticConfig) toHandle);
    } else {
      throw new ConfigurationException("Expected instance of " + DiagnosticConfig.class.getName() + ". Got: " + toHandle.getClass().getName());
    }
  }
 
  // --------------------------------------------------------------------------
  // Object overrides 
  
  public String toString() {
    return new StringBuffer("[").append(_name).append("]").toString();
  }

  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    _name = in.readUTF();
    _diagnosticConfig = (OptionalValue<DiagnosticConfig>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(_name);
    out.writeObject(_diagnosticConfig);
  }
}
