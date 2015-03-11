package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.ubik.util.Strings;

/**
 * Holds information about a shell script kept on the Corus server side.
 * 
 * @author yduchesne
 * 
 */
public class ShellScript extends AbstractPersistent<String, ShellScript> implements Externalizable {

  static final long serialVersionUID = 1L;

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
    this.alias = in.readUTF();
    this.description = in.readUTF();
    this.fileName = in.readUTF();

  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(alias);
    out.writeUTF(description);
    out.writeUTF(fileName);
  }

}
