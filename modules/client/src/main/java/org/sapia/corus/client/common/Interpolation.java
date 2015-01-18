package org.sapia.corus.client.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;

/**
 * Offers various variable interpolation methods.
 * 
 * @author yduchesne
 *
 */
public class Interpolation {

  private Interpolation() {
  }
  
  /**
   * This interpolates the variables in the given {@link Properties} instance. It performs interpolation
   * for as many passes as specified, over the given properties.
   * <p>
   * The passes can be necessary since values in the {@link Properties} instance are unordered, and this values
   * may corresponding to variables whose values are themselves in the passed in properties. For example, imagine
   * the following (processed in the suggested order):
   * <pre>
   * my.first.property=${my.second.property}
   * my.second.property=${my.third.property}
   * my.third.property=my-value
   * </pre>
   * In the above case, when interpolation is attempted for <code>my.first.property</code>, the value for the
   * ${my.second.property} is in fact resolved to <code>${my.third.property}</code>. It is only after a second
   * pass of rendering that ${my.third.property} will be finally interpolated, for the <code>my.first.property</code>.
   * 
   * @param input {@link Properties} instance whose values should be interpolated.
   * @param vars the {@link StrLookup} instance containing variable values to use for interpolation.
   * @param numberOfPasses the number of interpolation passes to perform.
   * @return the {@link Properties} instance resulting from the interpolation.
   */
  public static Properties interpolate(Properties input, StrLookup vars, int numberOfPasses) {
    Properties toReturn = new Properties();
    CompositeStrLookup composite = new CompositeStrLookup()
      .add(vars)
      .add(PropertiesStrLookup.getInstance(toReturn));
    StrSubstitutor subs = new StrSubstitutor(composite);
    
    for (int i = 0; i < numberOfPasses; i++) {
      for (String n : input.stringPropertyNames()) {
        String v = input.getProperty(n);
        if (v != null) {
          toReturn.setProperty(n, subs.replace(v));
        }
      }
    }
    return toReturn;
  }
  
  /**
   * Same as {@link #interpolate(Properties, StrLookup, int)}, but accepting a {@link Map} instance, and
   * returning a {@link Map}.
   * 
   * @param input {@link Map} instance whose values should be interpolated.
   * @param vars the {@link StrLookup} instance containing variable values to use for interpolation.
   * @param numberOfPasses the number of interpolation passes to perform.
   * @return the {@link Map} instance resulting from the interpolation.
   * @see #interpolate(Properties, StrLookup, int)
   */
  public static Map<String, String> interpolate(Map<String, String> input, StrLookup vars, int numberOfPasses) {
    Map<String, String> toReturn = new HashMap<String, String>();
    CompositeStrLookup composite = new CompositeStrLookup()
      .add(vars)
      .add(StrLookup.mapLookup(toReturn));
    StrSubstitutor subs = new StrSubstitutor(composite);
    
    for (int i = 0; i < numberOfPasses; i++) {
      for (String n : input.keySet()) {
        String v = input.get(n);
        if (v != null) {
          toReturn.put(n, subs.replace(v));
        }
      }
    }
    return toReturn;
  }
}

