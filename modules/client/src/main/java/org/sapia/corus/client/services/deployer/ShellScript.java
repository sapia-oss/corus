package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.ubik.util.Strings;

/**
 * Holds information about a shell script kept on the Corus server side.
 * 
 * @author yduchesne
 * 
 */
public class ShellScript extends AbstractPersistent<String, ShellScript> implements Externalizable, JsonStreamable, Mappable {

  static final long serialVersionUID = 1L;

  static final int VERSION_1       = 1;
  static final int CURRENT_VERSION = VERSION_1;
    
  private String alias, fileName, description;

  /**
   * Do not use: meant for externalization
   */
  public ShellScript() {
  }

  /**
   * @param alias
   *          the script's alias.
   * @param fileName
   *          the script's file name.
   * @param description
   *          the script's description.
   */
  public ShellScript(String alias, String fileName, String description) {
    this.alias = alias;
    this.fileName = fileName;
    this.description = description;
  }

  @Override
  @Transient
  public String getKey() {
    return alias;
  }

  /**
   * @return the script's alias.
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @return the script's description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the script's file name.
   */
  public String getFileName() {
    return fileName;
  }
  
  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("alias").value(alias)
      .field("fileName").value(fileName)
      .field("description").value(description)
    .endObject();
  }

  public static ShellScript fromJson(JsonInput in) {
    int classVersion = in.getInt("classVersion");
    if (classVersion == VERSION_1) {
      ShellScript script = new ShellScript(
          in.getString("alias"),
          in.getString("fileName"),
          in.getString("description")
      );
      return script;
    } else {
      throw new IllegalStateException("Version not handled: " + classVersion);
    }
  }

  // Mappable
  
  public java.util.Map<String,Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("script.alias", alias);
    toReturn.put("script.description", description);
    toReturn.put("script.fileName", fileName);
    return toReturn;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "alias", alias, "fileName", fileName, "description", description);
  }

  @Override
  public int hashCode() {
    return alias.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShellScript) {
      ShellScript other = (ShellScript) obj;
      return alias.equals(other.getAlias());
    }
    return false;
  }

  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      this.alias = in.readUTF();
      this.description = in.readUTF();
      this.fileName = in.readUTF();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);
    
    out.writeUTF(alias);
    out.writeUTF(description);
    out.writeUTF(fileName);
  }

}
