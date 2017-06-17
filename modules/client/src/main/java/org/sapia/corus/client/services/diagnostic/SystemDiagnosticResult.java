package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;

/**
 * Holds system diagnostic data.
 * 
 * @see SystemDiagnosticResult
 * 
 * @author yduchesne
 *
 */
public class SystemDiagnosticResult implements JsonStreamable, Externalizable {
  
  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private String                 componentName;
  private SystemDiagnosticStatus status;
  private String                 message;

  /**
   * Do not call: meant for externalization only.
   */
  public SystemDiagnosticResult() {
  }
  
  public SystemDiagnosticResult(String componentName, SystemDiagnosticStatus status, String message) {
    this.componentName = componentName;
    this.status        = status;
    this.message       = message;
  }  

  public SystemDiagnosticResult(String componentName, SystemDiagnosticStatus status) {
    this(componentName, status, status.getDefaultMessage());
  }  
  
  public String getComponentName() {
    return componentName;
  }
  
  public String getMessage() {
    return message;
  }
  
  public SystemDiagnosticStatus getStatus() {
    return status;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("component").value(componentName)
      .field("message").value(message)
      .field("status").value(status.name())
    .endObject();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      componentName = in.readUTF();
      status = (SystemDiagnosticStatus) in.readObject();
      message = in.readUTF();
    } else {
      throw new IllegalStateException("Version not handled: " + CURRENT_VERSION);
    }
    
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeUTF(componentName);
    out.writeObject(status);
    out.writeUTF(message);
  }
}
