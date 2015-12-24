package org.sapia.corus.client.services.deployer.dist;

import java.util.Collection;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.util.xml.confix.ConfigurationException;

/**
 * Holds assertion methods pertaining to the validation of distribution configuration.
 * 
 * @author yduchesne
 *
 */
public class ConfigAssertions {

  private ConfigAssertions() {
  }
  
  public static void attributeNotNull(String elementName, String attributeName, String attributeValue) throws ConfigurationException {
    if (attributeValue == null) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s not set for element <%s>",
          attributeName,
          elementName
      ));
    }
  }
  
  public static void attributeNotNullOrEmpty(String elementName, String attributeName, String attributeValue) throws ConfigurationException {
    attributeNotNull(elementName, attributeName, attributeValue);
    if (attributeValue.trim().length() == 0) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s is empty for element <%s>",
          attributeName,
          elementName
      ));
    }
  }
  
  public static void optionalAttributeNotNullOrEmpty(String elementName, String attributeName, OptionalValue<String> attributeValue) throws ConfigurationException {
    if (attributeValue.isSet()) {
      attributeNotNull(elementName, attributeName, attributeValue.get());
      if (attributeValue.get().trim().length() == 0) {
        throw new ConfigurationException(String.format(
            "Value of attribute %s is empty for element <%s>",
            attributeName,
            elementName
        ));
      }
    }
  }
  
  public static <N extends Number> void attributeGreater(String elementName, String attributeName, N minValue, N value) throws ConfigurationException {
    if (value == null) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s not set for element <%s>",
          attributeName,
          elementName
      ));
    } 
    if (value.doubleValue() <= minValue.doubleValue()) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s must be equal to or greater than %s for element <%s>. Got: %s",
          attributeName,
          minValue,
          elementName,
          value
      ));
    }
  }
  
  public static <N extends Number> void attributeAtLeast(String elementName, String attributeName, N minValue, N value) throws ConfigurationException {
    if (value == null) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s not set for element <%s>",
          attributeName,
          elementName
      ));
    } 
    if (value.doubleValue() < minValue.doubleValue()) {
      throw new ConfigurationException(String.format(
          "Value of attribute %s must be at least %s for element <%s>. Got: %s",
          attributeName,
          minValue,
          elementName,
          value
      ));
    }
  }
  
  public static void elementNotNull(String elementName, String elementValue) throws ConfigurationException {
    if (elementValue == null) {
      throw new ConfigurationException(String.format(
          "Value of element <%s> not set",
          elementName
      ));
    }
  }
  
  public static void elementNotNullOrEmpty(String elementName, String elementValue) throws ConfigurationException {
    elementNotNull(elementName, elementValue);
    if (elementValue.trim().length() == 0) {
      throw new ConfigurationException(String.format(
          "Value of element <%s> is empty",
          elementName
      ));
    }
  }
  
  public static void elementAtleast(String elementName, int minSize, Collection<?> toCheck) throws ConfigurationException {
    if (toCheck.size() < minSize) {
      throw new ConfigurationException(String.format(
          "At least %s instance(s) of element <%s> must be specified. Got %s elements",
          minSize,
          elementName,
          toCheck.size()
      ));
    }
  }
  
  public static void elementExpectsInstanceOf(String elementName, Class<?> expected, Object actual) throws ConfigurationException {
    if (!expected.isAssignableFrom(actual.getClass())) {
      throw new ConfigurationException(String.format(
          "Expected instance of %s under element <%s>. Got: %s", 
          expected.getName(), elementName, actual.getClass().getName()
      ));
    }
  }
  
}
