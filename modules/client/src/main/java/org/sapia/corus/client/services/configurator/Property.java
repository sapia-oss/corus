package org.sapia.corus.client.services.configurator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.ubik.util.Strings;

/**
 * Models a property, with a name, a value, and an optional category.
 * 
 * @author yduchesne
 *
 */
public class Property implements Externalizable, Comparable<Property>, Matcheable {
  
  private String name, value, category;

  /**
   * DO NOT INVOKE: meant for externalization only.
   */
  public Property() {
  }
  
  public Property(String name, String value, String category) {
    this.name     = name;
    this.value    = value;
    this.category = category;
  }
  
  /**
   * @return this instance's name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return this instance's value.
   */
  public String getValue() {
    return value;
  }
  
  /**
   * @return this instance's category, as an {@link OptionalValue}.
   */
  public OptionalValue<String> getCategory() {
    return OptionalValue.of(category);
  }
  
  // --------------------------------------------------------------------------
  // Comparable interface

  @Override
  public int compareTo(Property o) {
    int c =  name.compareTo(o.name);
    if (c == 0) {
      if (category == null && o.category == null) {
        // noop
      } else if (category == null) {
        c = -1;
      } else if (o.category == null) {
        c = 1;
      } else {
        c = category.compareTo(o.category);
      }
    } 
    return c;
  }
  
  // --------------------------------------------------------------------------
  // Matcheable interface
  
  @Override
  public boolean matches(Pattern pattern) {
    return value == null ? 
      pattern.matches(name) : 
      (pattern.matches(value) || pattern.matches(name));
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toString("name", name, "value", value, "category", category);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Property) {
      Property other = (Property) obj;
      return ObjectUtils.safeEquals(name, other.name) 
          && ObjectUtils.safeEquals(value, other.value)
          && ObjectUtils.safeEquals(category, other.category);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(name, value, category);
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    this.name     = in.readUTF();
    this.value    = in.readUTF();
    this.category = (String)in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeUTF(value);
    out.writeObject(category);
  }

}
