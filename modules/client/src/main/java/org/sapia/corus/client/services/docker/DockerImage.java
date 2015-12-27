package org.sapia.corus.client.services.docker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.util.Strings;


/**
 * Holds data about a Docker image.
 * 
 * @author yduchesne
 *
 */
public class DockerImage implements Externalizable, Mappable, JsonStreamable {
  
  private String      id;
  private String      creationTimeStamp;      
  private Set<String> tags = new HashSet<>();

  /**
   * DO NOT CALL: meant for externalization only.
   */
  public DockerImage() {
  }
  
  public DockerImage(String id, String creationTimeStamp) {
    this.id = id;
    this.creationTimeStamp = creationTimeStamp;
  }
  
  public String getId() {
    return id;
  }
  
  public Set<String> getTags() {
    return tags;
  }
  
  public String getCreationTimeStamp() {
    return creationTimeStamp;
  }

  // --------------------------------------------------------------------------
  // Mappable interface
  
  public Map<String, Object> asMap() {
    Map<String, Object> values = new HashMap<>();
    values.put("image.id", id);
    values.put("tags", tags);
    values.put("creationTimeStamp", creationTimeStamp);
    return values;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable interface
  
  public void toJson(JsonStream stream, JsonStreamable.ContentLevel level) {
    stream
      .beginObject()
        .field("id").value(id)
        .field("tags").strings(new ArrayList<>(tags))
        .field("creationTimeStamp").value(creationTimeStamp)
      .endObject();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
 
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    id                = (String) in.readObject();
    creationTimeStamp = (String) in.readObject();
    tags              = (Set<String>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(id);
    out.writeObject(creationTimeStamp);
    out.writeObject(tags);
  }

  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DockerImage) {
      DockerImage other = (DockerImage) obj;
      return ObjectUtil.safeEquals(id, other.id);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toString("id", id, "created", creationTimeStamp, "tags", tags);
  }
}
