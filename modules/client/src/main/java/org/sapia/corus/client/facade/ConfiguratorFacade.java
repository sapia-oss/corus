package org.sapia.corus.client.facade;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;

/**
 * This interface specifies a facade to the Corus {@link Configurator}
 * 
 * @author yduchesne
 * 
 */
public interface ConfiguratorFacade {

  /**
   * Adds the given property to the Corus server. If the {@link PropertyScope#PROCESS} scope is specified, the 
   * property is associated to these categories. If no categories are given, the property is associated
   * to the global process properties.
   * <p>
   * If the {@link PropertyScope#SERVER} scope is specified, the categories are ignored.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param name
   *          the name of the property to add.
   * @param value
   *          the value of the property to add.
   * @param categories
   *          the set of categories to which the property should be associated.
   */
  public void addProperty(PropertyScope scope, String name, String value, Set<String> categories, ClusterInfo cluster);

  /**
   * Adds the given properties to the Corus server. If the {@link PropertyScope#PROCESS} scope is specified, the 
   * given properties are associated to these categories. If no categories are given, the properties are associated
   * to the global process properties.
   * <p>
   * If the {@link PropertyScope#SERVER} scope is specified, the categories are ignored.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param props
   *          the {@link Properties} to add.
   * @param categories
   *          the set of categories to which the property should be associated.         
   * @param clearExisting
   *          if <code>true</code>, indicates that the existing properties
   *          should be cleared.
   */
  public void addProperties(PropertyScope scope, Properties props, Set<String> categories, boolean clearExisting, ClusterInfo cluster);

  /**
   * If the {@link PropertyScope#PROCESS} scope is specified, returns the global process properties, overwritten by 
   * the properties corresponding to the given categories (override will occur in the order in which the categories
   * are specified).
   * <p>
   * Otherwise, if the {@link PropertyScope#SERVER} scope is specified, returns the server properties, without consideration
   * for the given categories.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param categories
   *          a {@link List} of categories.
   * @return a {@link List} of {@link Property} instances.
   */
  public Results<List<Property>> getProperties(PropertyScope scope, List<String> categories, ClusterInfo cluster);

  /**
   * If the {@link PropertyScope#PROCESS} scope is specified, returns all the global process properties, and all the
   * category-specific properties.
   * <p>
   * If the {@link PropertyScope#SERVER} scope is specified, returns all the server properties.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @return a {@link List} of {@link Property} instances.
   */
  public Results<List<Property>> getAllProperties(PropertyScope scope, ClusterInfo cluster);
  
  /**
   * If the {@link PropertyScope#PROCESS} scope is specified, removes the specified process properties. If
   * no category is specified, removal is done from the global process properties. If category matchers are
   * specified, removal is done from the corresponding categories.
   * <p>
   * If the {@link PropertyScope#SERVER} scope is specified, removal is done from the server properties.
   * 
   * @param scope
   *          a {@link PropertyScope}
   * @param name
   *          the {@link Arg} instance corresponding to the name(s) of the property or properties to remove.
   * @param categories
   *          a {@link Set} of {@link Arg} instances used for matching from which categories the property or
   *          properties should be removed.
   */
  public void removeProperty(PropertyScope scope, Arg name, Set<Arg> categories, ClusterInfo cluster);

  /**
   * Adds the given tag to the Corus server.
   * 
   * @param tag
   *          a tag
   * @param cluster
   */
  public void addTag(String tag, ClusterInfo cluster);

  /**
   * Adds the given tags to the Corus server.
   * 
   * @param tags
   *          a {@link Set} of tags.
   * @param cluster
   */
  public void addTags(Set<String> tags, ClusterInfo cluster);

  /**
   * Removes the given tag from the Corus server.
   * 
   * @param tag
   *          a tag pattern.
   */
  public void removeTag(String tag, ClusterInfo cluster);

  /**
   * @param tags
   *          a {@link List} of {@link NameValuePair}s corresponding to the old
   *          vs new tag names.
   * @param cluster
   */
  public void renameTags(List<NameValuePair> tags, ClusterInfo cluster);

  /**
   * The tags of the Corus server.
   * 
   * @return a {@link Results} holding tags.
   */
  public Results<Set<Tag>> getTags(ClusterInfo cluster);

}
