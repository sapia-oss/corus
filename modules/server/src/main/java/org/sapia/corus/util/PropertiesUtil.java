package org.sapia.corus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;

/**
 * Encapsulates various properties methods.
 * 
 * @author yduchesne
 * 
 */
public class PropertiesUtil {

  /**
   * @param toFilter
   *          the {@link Properties} to filter.
   * @param filter
   *          the {@link PropertiesFilter} to use.
   * @return a new {@link Properties} instance, which will contain only the
   *         properties that will have been accepted by the given filter.
   */
  @SuppressWarnings("unchecked")
  public static Properties filter(Properties toFilter, PropertiesFilter filter) {
    Properties filtered = new Properties();
    Enumeration<String> names = (Enumeration<String>) toFilter.propertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = toFilter.getProperty(name);
      if (value != null && filter.accepts(name, value)) {
        filtered.setProperty(name, value);
      }
    }
    return filtered;
  }

  /**
   * Copies one set of properties to another.
   * 
   * @param from
   *          the {@link Properties} to copy form.
   * @param to
   *          the {@link Properties} to copy to.
   */
  @SuppressWarnings("unchecked")
  public static void copy(Properties from, Properties to) {
    Enumeration<String> names = (Enumeration<String>) from.propertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = from.getProperty(name);
      to.setProperty(name, value);
    }
  }

  /**
   * Transforms the given {@link Properties}, using the passed in transformer.
   * 
   * @param toTransform
   *          the {@link Properties} to transform.
   * @param transformer
   *          the {@link PropertiesTransformer} to use.
   */
  @SuppressWarnings("unchecked")
  public static void transform(Properties toTransform, PropertiesTransformer transformer) {
    Enumeration<String> names = (Enumeration<String>) toTransform.propertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = toTransform.getProperty(name);
      if (value != null && transformer.accepts(name, value)) {
        PropertiesTransformer.Property prop = transformer.transfrom(name, value);
        toTransform.setProperty(prop.name, prop.value);
      }
    }
  }

  /**
   * Populates a {@link Properties} instance from a given file.
   * 
   * @param toPopulate
   *          the {@link Properties} to populate.
   * @param toLoad
   *          the {@link File} containing the properties.
   * @throws IOException
   *           if an problem occurs trying to load the properties.
   */
  public static void load(Properties toPopulate, File toLoad) throws IOException {
    FileInputStream is = new FileInputStream(toLoad);
    try {
      toPopulate.load(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  /**
   * Populates a {@link Properties} instance from a given file, but only if the
   * file exists - returns silently if it doesn't.
   * 
   * @param toPopulate
   *          the {@link Properties} to populate.
   * @param toLoad
   *          the {@link File} containing the properties.
   * @throws IOException
   *           if an problem occurs trying to load the properties.
   */
  public static void loadIfExist(Properties toPopulate, File toLoad) throws IOException {
    if (toLoad.exists()) {
      load(toPopulate, toLoad);
    }
  }

  /**
   * Returns the property values contained in the given {@link Properties}
   * instance. These values are sorted by their corresponding property name.
   * 
   * @param props
   *          a {@link Properties} instance.
   * @return the {@link Collection} of sorted (by name) property values of the
   *         given {@link Properties}.
   */
  @SuppressWarnings("unchecked")
  public static Collection<String> values(Properties props) {
    Enumeration<String> names = (Enumeration<String>) props.propertyNames();
    Map<String, String> sortedProps = new TreeMap<String, String>();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = props.getProperty(name);
      if (value != null) {
        sortedProps.put(name, value);
      }
    }
    return sortedProps.values();
  }

  /**
   * Returns the sorted {@link Map} containing the given properties (sorting is
   * done by property name).
   * 
   * @param props
   *          a {@link Properties} instance.
   * @return the sorted {@link Map} corresponding to the given properties.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> map(Properties props) {
    Enumeration<String> names = (Enumeration<String>) props.propertyNames();
    Map<String, String> sortedProps = new TreeMap<String, String>();
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = props.getProperty(name);
      if (value != null) {
        sortedProps.put(name, value);
      }
    }
    return sortedProps;
  }

  /**
   * Performs variables substitution on the given property values.
   * 
   * @param props
   *          the {@link Properties} to process.
   * @param replacement
   *          the {@link StrLookup} instance which holds the replacement values.
   * @return a new {@link Properties} instance, containing the transformed
   *         properties.
   */
  @SuppressWarnings("unchecked")
  public static Properties replaceVars(Properties props, StrLookup replacement) {
    Enumeration<String> names = (Enumeration<String>) props.propertyNames();
    Properties toReturn = new Properties();
    StrSubstitutor subs = new StrSubstitutor(replacement);
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = props.getProperty(name);
      if (value != null) {
        toReturn.setProperty(name, subs.replace(value));
      }
    }
    return toReturn;
  }
  
  /**
   * @param scope a {@link PropertyScope}.
   * @param category the {@link OptionalValue} corresponding to an optional category to assign to the properties.
   * @param props the {@link Properties} to transform to a list of {@link Property} instances.
   * @return a {@link List} of {@link Property} instances.
   */
  public static List<Property> propertyList(PropertyScope scope, OptionalValue<String> category, Properties props) {
    List<Property> propList = new ArrayList<>(props.size());
    for (String n : props.stringPropertyNames()) {
      String v = props.getProperty(n);
      if (v != null) {
        propList.add(new Property(n, v, category.isNull() ? null : category.get()));
      }
    }
    return propList;
  }
  
  /**
   * Converts a {@link List} of {@link Property} instances to a {@link Properties} object,
   * discarding category information in the process.
   * 
   * @param propList a {@link List} of {@link Property} instances.
   * @return the {@link Properties} corresponding to the given list. 
   */
  public static Properties properties(List<Property> propList) {
    Properties props = new Properties();
    for (Property p : propList) {
      props.setProperty(p.getName(), p.getValue());
    }
    return props;
  }

  /**
   * Performs obfuscation of property value when it's a password.
   * 
   * @param name The name of the property.
   * @param value The value of the property.
   * @return The clear or obfuscated value.
   */
  public static String hideIfPassword(String name, String value) {
    if (name != null && name.toLowerCase().contains("password")) {
      return "********";
    } else {
      return value;
    }
  }

}
