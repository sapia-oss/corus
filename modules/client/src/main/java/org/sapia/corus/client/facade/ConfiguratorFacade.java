package org.sapia.corus.client.facade;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;

/**
 * This interface specifies a facade to the Corus {@link Configurator}
 * 
 * @author yduchesne
 *
 */
public interface ConfiguratorFacade {
  
  /**
   * Adds a given property to the Corus server.
   * @param scope a {@link PropertyScope}
   * @param name the name of the property to add.
   * @param value the value of the property to add.
   */
  public void addProperty(PropertyScope scope, String name, String value, ClusterInfo cluster);
  
  /**
   * @param scope a {@link PropertyScope}
   * @return the {@link Properties} held within the Corus server.
   */
  public Results<List<NameValuePair>> getProperties(PropertyScope scope, ClusterInfo cluster);
  
  /**
   * @param scope a {@link PropertyScope}
   * @param name the name of the property to remove.
   */
  public void removeProperty(PropertyScope scope, String name, ClusterInfo cluster);
  
  /**
   * Adds the given tag to the Corus server.
   * 
   * @param tag a tag
   * @param cluster
   */
  public void addTag(String tag, ClusterInfo cluster);

  /**
   * Adds the given tags to the Corus server.
   *  
   * @param tags a {@link Set} of tags.
   * @param cluster
   */
  public void addTags(Set<String> tags, ClusterInfo cluster);
  
  /**
   * Removes the given tag from the Corus server.
   * @param tag a tag pattern.
   */
  public void removeTag(String tag, ClusterInfo cluster);
  
  /**
   * The tags of the Corus server.
   * 
   * @return a {@link Results} holding tags.
   */
  public Results<Set<String>> getTags(ClusterInfo cluster);


}
