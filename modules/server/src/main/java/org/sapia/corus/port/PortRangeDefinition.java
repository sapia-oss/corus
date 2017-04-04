package org.sapia.corus.port;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;

/**
 * Defines a port range with a name, a lower bound and an upper bound.
 * 
 * @author jcdesrochers
 */
public class PortRangeDefinition extends AbstractPersistent<String, PortRangeDefinition> 
    implements Externalizable, Comparable<PortRangeDefinition>, JsonStreamable, Matcheable, Mappable {

  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;

  private String name;
  private int min;
  private int max;
  
  /**
   * Meant for externalization only.
   */
  public PortRangeDefinition() {
  }

  /**
   * Creates a new {@link PortRangeDefinition} instance with the arguments passed in.
   * 
   * @param name The name of the port range
   * @param min The min value of the port range
   * @param max The max value of the port range
   */
  public PortRangeDefinition(String name, int min, int max) {
    this.name = name;
    this.min = min;
    this.max = max;
  }

  /**
   * @return the name of this range.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the lower bound port in this range.
   */
  public int getMin() {
    return min;
  }

  /**
   * @return the upper bound port in this range.
   */
  public int getMax() {
    return max;
  }

  @Override
  public String getKey() {
    return name;
  }

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("range.name", name);
    toReturn.put("range.min", min);
    toReturn.put("range.max", max);
    return toReturn;
  }

  @Override
  public boolean matches(Pattern pattern) {
    return pattern.matches(name) || 
        pattern.matches(Integer.toString(min)) || 
        pattern.matches(Integer.toString(max));
  }

  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
        .field("classVersion").value(CURRENT_VERSION)
        .field("name").value(name)
        .field("min").value(min)
        .field("max").value(max)
        .endObject();
  }
  
  public static PortRangeDefinition fromJson(JsonInput in) {
    int inputVersion = in.getInt("classVersion");
    if (inputVersion == VERSION_1) {
      PortRangeDefinition def = new PortRangeDefinition(
          in.getString("name"),
          in.getInt("min"),
          in.getInt("max")
      );
      return def;

    } else {
      throw new IllegalStateException("Unhandled version: " + inputVersion);
    }
  }

  @Override
  public int compareTo(PortRangeDefinition other) {
    return getName().compareTo(other.getName());
  }

  @Override
  public String toString() {
    return new StringBuffer().append("[")
        .append("name=").append(name)
        .append(", min=").append(min)
        .append(", max=").append(max).append("]")
        .toString();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(CURRENT_VERSION);
    out.writeUTF(name);
    out.writeInt(min);
    out.writeInt(max);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      name = in.readUTF();
      min  = in.readInt();
      max  = in.readInt();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }

}
