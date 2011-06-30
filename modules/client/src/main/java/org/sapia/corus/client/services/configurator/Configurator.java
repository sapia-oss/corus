package org.sapia.corus.client.services.configurator;

import java.rmi.Remote;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;

/**
 * This interface specifies configuration behavior: it supports managing properties.
 * <p>
 * This interface extends {@link Remote}, and therefore implementations of it are available
 * remotely.
 * <p>
 * Since implementations of this interface may store passwords as property values, these values
 * are "hidden" when returned to the callers (a string of asterisks ('*') is returned in place of
 * such a value). 
 * <p>
 * By convention, the "hidden" properties should be determined by detecting the "password" character string
 * in their name.
 * 
 * @author yduchesne
 *
 */
public interface Configurator extends java.rmi.Remote, Module {

  String ROLE = Configurator.class.getName();

  public enum PropertyScope{
    PROCESS,
    SERVER;
  }
  
  /**
   * Adds a property.
   * 
   * @param scope a {@link PropertyScope}
   * @param name the name of the property
   * @param value the value of the property
   */
  public void addProperty(PropertyScope scope, String name, String value);
  
  /**
   * @param scope a {@link PropertyScope}
   * @param props the {@link Properties} to add
   * @param clearExisting if <code>true</code>, indicates that the existing properties should be cleared.
   */
  public void addProperties(PropertyScope scope, Properties props, boolean clearExisting);
  
  /**
   * @param scope a {@link PropertyScope}
   * @param name the name of the property to remove.
   */
  public void removeProperty(PropertyScope scope, Arg name);
 
  /**
   * Returns a property value.
   * 
   * @param name
   * @return the value corresponding to the given property name, or <code>null</code> if
   * no such value exists.
   */
  public String getProperty(String name);
  
  /**
   * Returns a copy of the properties held in this instance.
   * 
   * @param scope a {@link PropertyScope}
   * @return a {@link Properties} instance.
   */
  public Properties getProperties(PropertyScope scope);
  
  /**
   * Returns a list of name/value pairs corresponding to the properties held
   * in this instance.
   * 
   * @param scope a {@link PropertyScope}
   * @return a list of {@link org.sapia.corus.client.common.NameValuePair}
   */
  public List<NameValuePair> getPropertiesAsNameValuePairs(PropertyScope scope);
  
  /**
   * Adds a tag to this instance.
   * 
   * @param tag a {@link String}
   */
  public void addTag(String tag);
  
  /**
   * Adds the given tags to this instance.
   * 
   * @param tags a {@link Set} of tags.
   */
  public void addTags(Set<String> tags);

  /**
   * Removes the given tag from this instance.
   * 
   * @param tag a {@link String} corresponding to the tag to remove.
   */
  public void removeTag(String tag);
  
  /**
   * Removes the tags matching the given argument from this instance.
   * 
   * @param tag a {@link Arg} corresponding to the tags to remove.
   */
  public void removeTag(Arg tag);
  
  /**
   * @return a {@link Set} of {@link String}s corresponding to the tags
   * held by this instance.
   */
  public Set<String> getTags();
  
  /**
   * Clears all the tags held by this instance.
   */
  public void clearTags();
}
