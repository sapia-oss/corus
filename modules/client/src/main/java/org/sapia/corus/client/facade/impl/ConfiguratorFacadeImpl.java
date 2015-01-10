package org.sapia.corus.client.facade.impl;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Tag;

public class ConfiguratorFacadeImpl extends FacadeHelper<Configurator> implements ConfiguratorFacade {

  public ConfiguratorFacadeImpl(CorusConnectionContext context) {
    super(context, Configurator.class);
  }

  @Override
  public Results<List<Property>> getProperties(PropertyScope scope,
      List<String> categories, ClusterInfo cluster) {
    Results<List<Property>> results = new Results<List<Property>>();
    proxy.getPropertiesList(scope, categories);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public Results<List<Property>> getAllProperties(PropertyScope scope,
      ClusterInfo cluster) {
    Results<List<Property>> results = new Results<List<Property>>();
    proxy.getAllPropertiesList(scope);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public void addProperty(PropertyScope scope, String name, String value,
      Set<String> categories, ClusterInfo cluster) {
    proxy.addProperty(scope, name, value, categories);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void addProperties(PropertyScope scope, Properties props,
      Set<String> categories, boolean clearExisting, ClusterInfo cluster) {
    proxy.addProperties(scope, props, categories, clearExisting);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void removeProperty(PropertyScope scope, Arg name,
      Set<Arg> categories, ClusterInfo cluster) {
    proxy.removeProperty(scope, name, categories);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void addTag(String tag, ClusterInfo cluster) {
    proxy.addTag(tag);
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void addTags(Set<String> tags, ClusterInfo cluster) {
    proxy.addTags(tags);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public Results<Set<Tag>> getTags(ClusterInfo cluster) {
    Results<Set<Tag>> results = new Results<Set<Tag>>();
    proxy.getTags();
    invoker.invokeLenient(results, cluster);
    return results;
  }

  @Override
  public void removeTag(String tag, ClusterInfo cluster) {
    proxy.removeTag(ArgFactory.parse(tag));
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void renameTags(List<NameValuePair> tags, ClusterInfo cluster) {
    proxy.renameTags(tags);
    invoker.invokeLenient(void.class, cluster);
  }
}
