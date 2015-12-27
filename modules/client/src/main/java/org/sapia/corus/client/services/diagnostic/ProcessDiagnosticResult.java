package org.sapia.corus.client.services.diagnostic;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.util.Assertions;

/**
 * Holds diagnostic data for a given process/port.
 * 
 * @author yduchesne
 *
 */
public class ProcessDiagnosticResult implements Externalizable, JsonStreamable {
  
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private Process                   process;
  private OptionalValue<String>     protocol = OptionalValue.none();
  private OptionalValue<ActivePort> port     = OptionalValue.none();
  private ProcessDiagnosticStatus   status;
  private String                    message;
  
  public ProcessDiagnosticResult() {
  }
  
  public ProcessDiagnosticResult(ProcessDiagnosticStatus status, String message, Process process, String protocol, ActivePort port) {
    this(status, message,  process, OptionalValue.of(protocol), port);
  }
  
  public ProcessDiagnosticResult(ProcessDiagnosticStatus status, String message, Process process, OptionalValue<String> protocol, ActivePort port) {
    this(status, message,  process);
    this.protocol = protocol;
    this.port     = OptionalValue.of(port);
  }
  
  public ProcessDiagnosticResult(ProcessDiagnosticStatus status, String message, Process process) {
    this.status  = status;
    this.message = message;
    this.process = process;
  }

  public ProcessDiagnosticStatus getStatus() {
    return status;
  }
  
  public void flagSuspect() {
    Assertions.isTrue(
        status == ProcessDiagnosticStatus.CHECK_FAILED, 
        "Current status must be CHECK_FAILED for SUSPECT status to be flagged"
    );
  }
  
  public String getMessage() {
    return message;
  }
  
  public Process getProcess() {
    return process;
  }
  
  public OptionalValue<ActivePort> getPort() {
    return port;
  }
  
  public OptionalValue<String> getProtocol() {
    return protocol;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(process, port, protocol, status, message);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessDiagnosticResult) {
      ProcessDiagnosticResult other = (ProcessDiagnosticResult) obj;
      return ObjectUtil.safeEquals(process, other.process)
          && ObjectUtil.safeEquals(port, other.port)
          && ObjectUtil.safeEquals(protocol, other.protocol)
          && ObjectUtil.safeEquals(status, other.status)
          && ObjectUtil.safeEquals(message, other.message);
    }
    return false;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("status").value(status.name())
      .field("message").value(message);
    
    if (port.isSet()) {
      stream.field("diagnosticPort")
        .beginObject()
          .field("name").value(port.get().getName())
          .field("value").value(port.get().getPort())
        .endObject();
    }
    if (protocol.isSet()) {
      stream.field("protocol").value(protocol.get());
    }
    stream.field("process");
    process.toJson(stream, level);
    stream.endObject();
  }

  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    
    int inputVersion = in.readInt();
    
    if (inputVersion == VERSION_1) {
      process  = (Process) in.readObject();
      protocol = (OptionalValue<String>) in.readObject();
      port     = (OptionalValue<ActivePort>) in.readObject();
      status   = (ProcessDiagnosticStatus) in.readObject();
      message  = in.readUTF();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
      
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);
    
    out.writeObject(process);
    out.writeObject(protocol);
    out.writeObject(port);
    out.writeObject(status);
    out.writeUTF(message);
  }

}