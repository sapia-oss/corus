package org.sapia.corus.client.services.configurator;

import java.rmi.Remote;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.Dumpable;
import org.sapia.corus.client.services.database.RevId;

/**
 * This interface specifies configuration behavior: it supports managing
 * properties.
 * <p>
 * This interface extends {@link Remote}, and therefore implementations of it
 * are available remotely.
 * <p>
 * Since implementations of this interface may store passwords as property
 * values, these values are "hidden" when returned to the callers (a string of
 * asterisks ('*') is returned in place of such a value).
 * <p>
 * By convention, the "hidden" properties should be determined by detecting the
 * "password" character string in their name.
 * 
 * @author yduchesne
 * 
 */
public interface Configurator extends java.rmi.Remote, Module, Dumpable {

  String ROLE = Configurator.class.getName();

  public enum PropertyScope {
    PROCESS, SERVER;

    /**
     * @param name a scope name.
     * @return the {@link PropertyScope} corresponding to the given name.
     */
    public static final PropertyScope forName(String name) {
      if (name.startsWith("p")) {
        return PROCESS;
      } else if (name.startsWith("s")) {
        return SERVER;
      } else {
        throw new IllegalArgumentException("Expected either p or s, got: " + name);
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Adds a property.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param name
   *          the name of the property
   * @param value
   *          the value of the property
   * @param categories
   *          the {@link Set} of categories to which to associate the given property.
   */
  public void addProperty(PropertyScope scope, String name, String value, Set<String> categories);

  /**
   * @param scope
   *          a {@link PropertyScope}
   * @param props
   *          the {@link Properties} to add
   * @param categories
   *          the {@link Set} of categories to which to associate the given properties.
   * @param clearExisting
   *          if <code>true</code>, indicates that the existing properties
   *          should be cleared.
   */
  public void addProperties(PropertyScope scope, Properties props, Set<String> categories, boolean clearExisting);
  
  /**
   * Returns a property value, for the first category that matches. This method first looks in the
   * properties with the {@link PropertyScope#SERVER} scope. If no corresponding value is found there,
   * the {@link PropertyScope#PROCESS} scope is searched, in the order specified by the given categories.
   * <p>
   * In the above case, properties with given names found in subsequent categories override properties
   * with the same name in previous categories.
   * <p>
   * If no property is found for the given property, the global process properties are searched.
   * 
   * @param name
   * @param categories 
   *         the {@link List} of categories for which to get the corresponding property.
   * @return the value corresponding to the given property name, or
   *         <code>null</code> if no such value exists.
   */
  public String getProperty(String name, List<String> categories);
  
  /**
   * Returns a copy of the properties held in this instance. Properties with given names found in 
   * subsequent categories override properties with the same name in previous categories.
   * <p>
   * For any property for which no category match is found, the global process properties are ultimately
   * looked up.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param categories 
   *         the {@link List} of categories for which to return the corresponding properties.
   * @return a {@link Properties} instance.
   */
  public Properties getProperties(PropertyScope scope, List<String> categories);

  /**
   * Returns a list of name/value pairs corresponding to the properties held in this instance.
   * <p>
   * Properties with given names found in subsequent categories override properties with the 
   * same name in previous categories.
   * <p>
   * If no categories are specified, only the global process properties are returned.
   * <p>
   * By the same token, if categories are specified but none matches anyone of the given 
   * categories, the global process properties are returned.
   *  
   * @param scope
   *          a {@link PropertyScope}
   * @param categories 
   *          a {@link List} of categories for which to return the corresponding properties.
   * @return  a list of {@link Property} instances.
   */
  public List<Property> getPropertiesList(PropertyScope scope, List<String> categories);
  
  /**
   * This method returns all the properties, provided they're matched by the given category matchers 
   * (this method does not do any form of override, as in the {@link #getPropertiesList(PropertyScope, List)}
   * method.
   * 
   * @param scope
   *          a {@link PropertyScope}.
   * @param categories the set of category matchers to use.
   * @return the {@link List} of all {@link Property} instances, matched by the given category matchers.
   */
  public List<Property> getAllPropertiesList(PropertyScope scope, Set<ArgMatcher> categories);
  
  /**
   * @param scope
   *          a {@link PropertyScope}
   * @param name
   *          the name of the property to remove.
   * @param categories 
   *          the {@link Set} of category matchers for which to remove the corresponding properties.
   */
  public void removeProperty(PropertyScope scope, ArgMatcher name, Set<ArgMatcher> categories);

  /**
   * Archives all process properties.
   * 
   * @param revID the revision ID to use when archiving.
   */
  public void archiveProcessProperties(RevId revId);
  
  /**
   * Unarchives all process properties.
   * 
   * @param revId the ID of the revision to unarchive.
   */
  public void unarchiveProcessProperties(RevId revId);
  
  // --------------------------------------------------------------------------

  /**
   * Adds a tag to this instance.
   * 
   * @param tag
   *          a {@link String}
   */
  public void addTag(String tag);

  /**
   * Adds the given tags to this instance.
   * 
   * @param tags
   *          a {@link Set} of tags.
   * @param clearExisting
   *          <code>true</code> if the current tags should be deleted.
   */
  public void addTags(Set<String> tags, boolean clearExisting);

  /**
   * Replaces the tags corresponding to the names in the given name-value pairs
   * with their value counterpart.
   * 
   * @param tags
   *          the {@link List} of {@link NameValuePair}s holding the old tag vs
   *          new tag names.
   */
  public void renameTags(List<NameValuePair> tags);

  /**
   * Removes the given tag from this instance.
   * 
   * @param tag
   *          a {@link String} corresponding to the tag to remove.
   */
  public void removeTag(String tag);

  /**
   * Removes the tags matching the given argument from this instance.
   * 
   * @param tag
   *          a {@link ArgMatcher} corresponding to the tags to remove.
   */
  public void removeTag(ArgMatcher tag);

  /**
   * @return a {@link Set} of {@link Tag}s corresponding to the tags held by
   *         this instance.
   */
  public Set<Tag> getTags();

  /**
   * Clears all the tags held by this instance.
   */
  public void clearTags();
  
  /**
   * Archives all tags.
   * 
   * @param revID the revision ID to use when archiving.
   */
  public void archiveTags(RevId revId);
  
  /**
   * Unarchives all tags.
   * 
   * @param revId the ID of the revision to unarchive.
   */
  public void unarchiveTags(RevId revId);
}
