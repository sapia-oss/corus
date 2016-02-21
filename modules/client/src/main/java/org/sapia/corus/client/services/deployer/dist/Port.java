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

  private String name;
  private OptionalValue<? extends DiagnosticConfig> diagnosticConfig = OptionalValue.none();
  private OptionalValue<PublishingConfig>           publishing = OptionalValue.none();

  /** Creates a new instance of Port */
  public Port() {
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  public OptionalValue<? extends DiagnosticConfig> getDiagnosticConfig() {
    return diagnosticConfig;
  }
  
  public OptionalValue<PublishingConfig> getPublishing() {
    return publishing;
  }
  
  public PublishingConfig createPublishing() {
    if (publishing.isNull()) {
      publishing = OptionalValue.of(new PublishingConfig());
    }
    return publishing.get();
  }
  
  // --------------------------------------------------------------------------
  // Configuration unmarshalling
  
  public Object onCreate() throws ConfigurationException {
    ConfigAssertions.attributeNotNullOrEmpty("port", "name", name);
    return this;
  }
  
  @Override
  public void handleObject(String name, Object toHandle)
      throws ConfigurationException {
    ConfigAssertions.elementExpectsInstanceOf("port", DiagnosticConfig.class, toHandle);
    diagnosticConfig = OptionalValue.of((DiagnosticConfig) toHandle);
  }
 
  // --------------------------------------------------------------------------
  // Object overrides 
  
  public String toString() {
    return new StringBuffer("[").append(name).append("]").toString();
  }

  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    name = in.readUTF();
    diagnosticConfig = (OptionalValue<DiagnosticConfig>) in.readObject();
    publishing = (OptionalValue<PublishingConfig>) in.readObject();

  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeObject(diagnosticConfig);
    out.writeObject(publishing);
  }
}
