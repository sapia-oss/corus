package org.sapia.corus.configurator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;

/**
 * Models a configuration property, corresponding to a name and its associated
 * value.
 * 
 * @author yduchesne
 * 
 */
public class ConfigProperty extends AbstractPersistent<String, ConfigProperty> implements JsonStreamable, Externalizable {

  static final long serialVersionUID = 1L;
  
  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;

  private int classVersion = CURRENT_VERSION;
  private String name, value;

  ConfigProperty() {
  }

  ConfigProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  @Override
  public String getKey() {
    return getName();
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfigProperty) {
      ConfigProperty other = (ConfigProperty) obj;
      return ObjectUtils.safeEquals(other.name, name) && ObjectUtils.safeEquals(other.value, value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(name, value);
  }
  
  @Override
  public String toString() {
    return value;
  }

  // --------------------------------------------------------------------------
  // JsonStreamable

  
  @Override
  public void toJson(JsonStream stream) {
    stream.beginObject()
      .field("classVersion").value(classVersion)
      .field("name").value(name)
      .field("value").value(value)
    .endObject();
  }
  
  /**
   * @param input the {@link JsonInput} from which to create a new instance of this class.
   * @return a new {@link ConfigProperty} instance.
   */
  public static ConfigProperty fromJson(JsonInput input) {
    int inputVersion = input.getInt("classVersion");
    if (inputVersion == VERSION_1) {
      ConfigProperty prop = new ConfigProperty(
          input.getString("name"),
          input.getString("value")
      );
      return prop;
    } 
    throw new IllegalStateException("Version not handled: " + inputVersion);
  }
 
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      name = in.readUTF();
      value = in.readUTF();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(classVersion);
    out.writeUTF(name);
    out.writeUTF(value);
  }

}
