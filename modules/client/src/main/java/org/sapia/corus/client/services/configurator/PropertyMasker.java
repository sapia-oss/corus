package org.sapia.corus.client.services.configurator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * Utility class that holds 
 * 
 * @author yduchesne
 *
 */
public class PropertyMasker implements Externalizable {

  public static final String DEFAULT_MASK = "*********";
  
  private List<ArgMatcher> matchers = new ArrayList<ArgMatcher>();
  
  private String mask;
  
  /**
   * @param mask the value to us
   */
  public PropertyMasker(String mask) {
    this.mask = mask;
  }
  
  public PropertyMasker() {
    this(DEFAULT_MASK);
  }

  /**
   * @return a new instance of this class, initialized with default property name patterns.
   */
  public static PropertyMasker newDefaultInstance() {
    return new PropertyMasker().addMatcher("*password*", "*username*", "*secret*", "*access*key*");
  }
  
  /**
   * @param someMatchers one or more {@link ArgMatcher}s to add to this instance.
   * 
   * @return this instance.
   */
  public PropertyMasker addMatcher(ArgMatcher...someMatchers) {
    matchers.addAll(Arrays.asList(someMatchers));
    return this;
  }
  
  /**
   * @param patterns one or more patterns to add to this instance.
   * 
   * @return this instance.
   */
  public PropertyMasker addMatcher(String...patterns) {
    matchers.addAll(Collects.convertAsList(Arrays.asList(patterns), new Func<ArgMatcher, String>() {
      @Override
      public ArgMatcher call(String pattern) {
        return ArgMatchers.parse(pattern);
      }
    }));
    return this;
  }
  
  /**
   * @param propertyName
   * @return <code>true</code> of the given property name corresponds to a property
   * that should be hidden.
   */
  public boolean isHidden(String propertyName) {
    for (ArgMatcher m : matchers) {
      if (m.matches(propertyName)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * @param propertyName the name of the property name.
   * @param propertyValue the property value to mask, if need be.
   * @return a masked value if the given property value should be hidden, or the original
   * value if not.
   */
  public String getMaskedValue(String propertyName, String propertyValue) {
    if (isHidden(propertyName)) {
      return mask;
    }
    return propertyValue;
  }
  
  /**
   * @param property a {@link Property} to mask or not, depending.
   * @return a new {@link Property} instance with a masked value, if it should be hidden, or 
   * the instance passed in if not.
   */
  public Property getMaskedProperty(Property property) {
    if (isHidden(property.getName())) {
      return new Property(property.getName(), mask, property.getCategory());
    }
    return property;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    mask = in.readUTF();
    matchers = (List<ArgMatcher>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(mask);
    out.writeObject(matchers);
  }

}
