package org.sapia.corus.client.services.deployer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.util.Strings;

/**
 * Holds information about a file.
 * 
 * @author yduchesne
 * 
 */
public class FileInfo implements Externalizable, JsonStreamable {

  static final long serialVersionUID = 1L;

  private String fileName;
  private long   length;
  private Date   lastModified;

  /**
   * Do not call: meant for externalization only.
   */
  public FileInfo() {
  }

  public FileInfo(String fileName, long length, Date lastModified) {
    this.fileName = fileName;
    this.length = length;
    this.lastModified = lastModified;
  }

  /**
   * @return this instance's corresponding file length.
   */
  public long getLength() {
    return length;
  }

  /**
   * @return this instance's corresponding file name.
   */
  public String getName() {
    return fileName;
  }

  /**
   * @return the date of the file's last modification.
   */
  public Date getLastModified() {
    return lastModified;
  }

  @Override
  public int hashCode() {
    return fileName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileInfo) {
      return ((FileInfo) obj).fileName.equals(fileName);
    }
    return false;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "fileName", fileName);
  }

  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("name").value(fileName)
      .field("lastModifiedTimeMillis").value(lastModified.getTime())
      .field("lastModifiedTimestamp").value(lastModified)
      .field("length").value(length)
    .endObject();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.fileName = (String) in.readObject();
    this.length = in.readLong();
    this.lastModified = (Date) in.readObject();

  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(fileName);
    out.writeLong(length);
    out.writeObject(lastModified);
  }

}
