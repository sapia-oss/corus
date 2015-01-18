package org.sapia.corus.client.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.ubik.util.Assertions;

/**
 * Provide utility methods for handling {@link StrLookup} instances.
 * 
 * @author yduchesne
 *
 */
public class StrLookups {

  private StrLookups() {
  }
  
  /**
   * Merges the given {@link StrLookup} instances into a single {@link CompositeStrLookup}.
   * 
   * @param lookups Multiple {@link StrLookup} instances.
   * @return a new {@link CompositeStrLookup} instance.
   */
  public static CompositeStrLookup merge(StrLookup...lookups) {
    CompositeStrLookup toReturn = new CompositeStrLookup();
    for (StrLookup l : lookups) {
      toReturn.add(l);
    }
    return toReturn;
  }
  
  /**
   * @param keyValues expects an array corresponding to a sequence of key/value pairs.
   * @return a new {@link StrLookup} instance.
   */
  public static StrLookup forKeyValues(String...keyValues) {
    Assertions.isTrue(keyValues.length % 2 == 0, "Given input must correspond to a series of key-value pairs (odd number of entries provided)");
    Map<String, String> toReturn = new HashMap<String, String>();
    for (int i = 0; i < keyValues.length; i+=2) {
      toReturn.put(keyValues[i], keyValues[i + 1]);
    }
    return StrLookup.mapLookup(toReturn);
  }
  
}
