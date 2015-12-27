package org.sapia.corus.client.services.docker;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.audit.AuditInfo;

/**
 * Requests the return of a Docker image from Corus.
 * 
 * @author yduchesne
 *
 */
public class DockerSaveRequest extends DockerRequestSupport {
  
  private String    imageName;
  
  /**
   * DO NOT CALL: meant for externalization only.
   */
  public DockerSaveRequest() {
  }
  
  public DockerSaveRequest(AuditInfo auditInfo, String imageName) {
    super(auditInfo);
    this.imageName = imageName;
  }
  
  public String getImageName() {
    return imageName;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    imageName = in.readUTF();
  }
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(imageName);
  }

}
