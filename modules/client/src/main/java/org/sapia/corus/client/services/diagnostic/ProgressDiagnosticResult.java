package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;

/**
 * Holds the result of progress diagnostics.
 * 
 * @author yduchesne
 *
 */
public class ProgressDiagnosticResult implements Externalizable, JsonStreamable {
  
  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;

  private List<String> errorMessages;
  
  /**
   * DO NOT INVOKE: meant for externalization.
   */
  public ProgressDiagnosticResult() {
  }
  
  public ProgressDiagnosticResult(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }
  
  /**
   * @return a {@link List} of error messages.
   */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("errors").strings(errorMessages)
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
      errorMessages = (List<String>) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + CURRENT_VERSION);
    }
    
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeObject(errorMessages);
  }
}
