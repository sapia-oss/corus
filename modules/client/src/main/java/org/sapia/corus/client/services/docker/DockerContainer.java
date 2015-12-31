package org.sapia.corus.client.services.docker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.ubik.util.Strings;

public class DockerContainer implements Externalizable {
  
  private String id, imageName, creationTimeStamp;
  private List<String> names = new ArrayList<String>();
  
  /**
   * DO NOT CALL: meant for externalization only
   */
  public DockerContainer() {
  }
  
  public DockerContainer(String id, String imageName, String creationTimeStamp) {
    this.id                = id;
    this.imageName         = imageName;
    this.creationTimeStamp = creationTimeStamp;
  }
  
  public String getId() {
    return id;
  }
  
  public String getImageName() {
    return imageName;
  }
  
  public List<String> getNames() {
    return names;
  }
  
  public String getCreationTimeStamp() {
    return creationTimeStamp;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(id);
    out.writeObject(imageName);
    out.writeObject(creationTimeStamp);
    out.writeObject(names);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    id = (String) in.readObject();
    imageName = (String) in.readObject();
    creationTimeStamp = (String) in.readObject();
    names = (List<String>) in.readObject();
  }
    
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DockerContainer) {
      DockerContainer other = (DockerContainer) obj;
      return ObjectUtil.safeEquals(id, other.id);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(
        this,
        "id", id,
        "imageName", imageName,
        "names", names
    );
  }
 

}
