package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.UUID;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;

/**
 * Groups processes that have been started in the same command (through the -i option).
 * 
 * @author yduchesne
 *
 */
public class ProcessStartupInfo implements Externalizable, JsonStreamable {
  
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private long   requestedAt     = System.currentTimeMillis();
  private String id              = UUID.randomUUID().toString();
  private int    requestedInstances;
  
  /**
   * DO NOT CALL: meant for externalization only.
   */
  public ProcessStartupInfo() {
  }
  
  public ProcessStartupInfo(int requestedInstances) {
    this.requestedInstances = requestedInstances;
  }
  
  public String getStartGroupId() {
    return id;
  }    

  public long getRequestedAt() {
    return requestedAt;
  }
 
  public int getRequestedInstances() {
    return requestedInstances;
  }
  
  public static ProcessStartupInfo forSingleProcess() {
    return new ProcessStartupInfo(1);
  }
  
  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("requestedAtMillis").value(requestedAt)
      .field("id").value(id)
      .field("requestedInstances").value(requestedInstances)
   .endObject();
  }
  
  public static ProcessStartupInfo fromJson(JsonInput input) {
    ProcessStartupInfo info = new ProcessStartupInfo();
    int inputVersion = input.getInt("classVersion");
    if (inputVersion == VERSION_1) {
      info.id = input.getString("id");
      info.requestedAt = input.getLong("requestedAtMillis");
      info.requestedInstances = input.getInt("requestedInstances");
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
    
    return info;
  } 

  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessStartupInfo) {
      ProcessStartupInfo other = (ProcessStartupInfo) obj;
      return ObjectUtil.safeEquals(id, other.id)
          && ObjectUtil.safeEquals(requestedAt, other.requestedAt)
          && ObjectUtil.safeEquals(requestedInstances, other.requestedInstances);
    } 
    return false;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    switch (inputVersion) {
      case VERSION_1:
        requestedAt = in.readLong();
        id          = in.readUTF();
        requestedInstances = in.readInt();
        break;
      default: 
        throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  public void writeExternal(java.io.ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeLong(requestedAt);
    out.writeUTF(id);
    out.writeInt(requestedInstances);
  }
  
}
