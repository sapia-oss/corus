package org.sapia.corus.client.services.configurator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.common.Matcheable;

/**
 *  Encapsulates a tag value.
 *  
 * @author yduchesne
 *
 */
public class Tag implements Comparable<Tag>, Matcheable, Externalizable {
  
  static final long serialVersionUID = 1L;
  
  private String value;
  
  /**
   * Do not call: meant for externalization only.
   */
  public Tag() {
  }
  
  public Tag(String value) {
    this.value = value;
  }
  
  /**
   * @return this instance's value.
   */
  public String getValue() {
    return value;
  }
  
  // --------------------------------------------------------------------------
  // Matcheable interface
  
  
  @Override
  public boolean matches(Pattern pattern) {
    return pattern.matches(value);
  }
  
  // --------------------------------------------------------------------------
  // Comparable interface
  
  @Override
  public int compareTo(Tag t) {
    return value.compareTo(t.value);
  }
  
  // --------------------------------------------------------------------------
  // Static methods
  
  /**
   * @param tagValues a {@link Set} of tag values.
   * @return the {@link Set} of {@link Tag} instances corresponding to the given values.
   */
  public static Set<Tag> asTags(Set<String> tagValues) {
    Set<Tag> toReturn = new HashSet<>();
    for (String t : tagValues) {
      toReturn.add(new Tag(t));
    }
    return toReturn;
  }
  
  /**
   * @param tags a {@link Set} of tags to convert.
   * @return the {@link Set} of values corresponding to the given tags.
   */
  public static Set<String> asStrings(Set<Tag> tags) {
    Set<String> values = new HashSet<>();
    for (Tag t : tags) {
      values.add(t.getValue());
    }
    return values;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {  
    value = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(value);
  }

  // --------------------------------------------------------------------------
  // Object overrides 
  
  @Override
  public int hashCode() {
    return value.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Tag) {
      return ((Tag) obj).value.equals(value);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return value;
  }

}
