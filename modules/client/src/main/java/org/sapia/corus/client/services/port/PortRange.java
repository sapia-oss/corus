/*
 * PortRange.java
 *
 * Created on October 18, 2005, 6:46 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.sapia.corus.client.services.port;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;

/**
 * @author yduchesne
 */
public class PortRange extends AbstractPersistent<String, PortRange> 
  implements Externalizable, Comparable<PortRange>, JsonStreamable, Matcheable, Mappable {

  static final long serialVersionUID = 1L;

  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private String name;
  private int min, max;
  private List<Integer> available = new ArrayList<Integer>();
  private List<Integer> active    = new ArrayList<Integer>();
  
  /**
   * Meant for externalization only.
   */
  public PortRange() {
  }
  
  @Override
  @Transient
  public String getKey() {
    return name;
  }

  /** Creates a new instance of PortRange */
  public PortRange(String name, int min, int max) throws PortRangeInvalidException {
    if (min <= 0)
      throw new PortRangeInvalidException("Min port must be greater than 0");
    if (max <= 0)
      throw new PortRangeInvalidException("Max port must be greater than 0");
    if (min > max)
      throw new PortRangeInvalidException("Min port must be lower than max port");

    this.name = name;
    this.min  = min;
    this.max  = max;

    for (int i = min; i <= max; i++) {
      available.add(new Integer(i));
    }
  }

  public String getName() {
    return name;
  }

  public List<Integer> getAvailable() {
    return available;
  }

  public List<Integer> getActive() {
    return active;
  }

  /**
   * @return the lowerbound port in this range.
   */
  public int getMin() {
    return min;
  }

  /**
   * @return the higherbound port in this range.
   */
  public int getMax() {
    return max;
  }

  /**
   * @param port
   *          a port to release.
   */
  public synchronized void release(int port) {
    Integer portObj = new Integer(port);
    active.remove(portObj);
    if (!available.contains(portObj)) {
      available.add(portObj);
    }
    Collections.sort(available);
  }

  /**
   * acquires an available port from this instance.
   */
  public synchronized int acquire() throws PortUnavailableException {
    if (available.size() == 0) {
      throw new PortUnavailableException("No port available for range: " + name);
    }
    Integer portObj = (Integer) available.remove(0);
    active.add(portObj);
    Collections.sort(active);
    return portObj.intValue();
  }

  /**
   * @return <code>true</code> if this range has busy ports.
   */
  public boolean hasBusyPorts() {
    return active.size() > 0;
  }

  /**
   * releases all active ports.
   */
  public synchronized void releaseAll() {
    for (int i = 0; i < active.size(); i++) {
      Integer busy = (Integer) active.get(i);
      if (!available.contains(busy)) {
        available.add(busy);
      }
    }
    Collections.sort(available);
    active.clear();
  }
  
  /**
   * @return <code>true</code> if the given port range is conflicting with this
   *         instance.
   */
  public boolean isConflicting(PortRange other) {
    return (other.max <= max && other.max >= min) || (other.min >= min && other.min <= max);
  }
  
  @Override
  public synchronized void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("name").value(name)
      .field("min").value(min)
      .field("max").value(max)
      .field("availablePorts").numbers(available)
      .field("activePorts").numbers(active)
    .endObject();
  }
  
  public static PortRange fromJson(JsonInput in) {
    try {

      int inputVersion = in.getInt("classVersion");
      if (inputVersion == VERSION_1) {
        PortRange pr = new PortRange(
            in.getString("name"),
            in.getInt("min"),
            in.getInt("max")
        );
        pr.available.clear();
        pr.available.addAll(Arrays.asList(in.getIntObjectArray("availablePorts")));
        pr.active.addAll(Arrays.asList(in.getIntObjectArray("activePorts")));
        return pr;
      } else {
        throw new IllegalStateException("Unhandled version: " + inputVersion);
      }
    } catch (PortRangeInvalidException e) {
      throw new IllegalStateException("Could not read PortRange from JSON", e);
    }
  }

  @Override
  public boolean matches(Pattern pattern) {
    return pattern.matches(name) || 
        pattern.matches(Integer.toString(min)) || 
        pattern.matches(Integer.toString(max));
  }
  
  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("range.name", name);
    toReturn.put("range.min", min);
    toReturn.put("range.max", max);
    toReturn.put("range.available", available);
    toReturn.put("range.active", active);
    return toReturn;
  }
  
  @Override
  public int compareTo(PortRange other) {
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

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      name = in.readUTF();
      min  = in.readInt();
      max  = in.readInt();
      available = (List<Integer>) in.readObject();
      active    = (List<Integer>) in.readObject();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);
    
    out.writeUTF(name);
    out.writeInt(min);
    out.writeInt(max);
    out.writeObject(available);
    out.writeObject(active);
  }
}
