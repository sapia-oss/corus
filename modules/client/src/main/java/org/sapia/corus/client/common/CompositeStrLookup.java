package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrLookup;

/**
 * Implements the composite pattern by extending the {@link StrLookup} class and
 * allowing adding {@link StrLookup} instances that are traversed upon lookup to
 * resolved given variable values, in the order in which they where added.
 * <p>
 * An instance of this class can be configured as "lenient" (see {@link #lenient()}: 
 * the instance will return the name of a value, if that value itself cannot be found.
 * 
 * @author yduchesne
 * 
 */
public class CompositeStrLookup extends StrLookup {

  private List<StrLookup> nested = new ArrayList<StrLookup>();

  private boolean lenient = false;
  
  /**
   * @param lookup
   *          a {@link StrLookup} to add.
   * @return this instance.
   */
  public CompositeStrLookup add(StrLookup lookup) {
    nested.add(lookup);
    return this;
  }
  
  /**
   * Set's this instance as lenient.
   * 
   * @return this instance.
   */
  public CompositeStrLookup lenient() {
    lenient = true;
    return this;
  }

  @Override
  public String lookup(String name) {
    for (StrLookup n : nested) {
      String value = n.lookup(name);
      if (value != null) {
        return value;
      }
    }
    if (lenient) {
      return "${" + name + "}";
    }
    return null;
  }

}
