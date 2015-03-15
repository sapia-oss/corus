package org.sapia.corus.client.facade.impl;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.database.RevId;

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
      Set<ArgMatcher> categories, ClusterInfo cluster) {
    Results<List<Property>> results = new Results<List<Property>>();
    proxy.getAllPropertiesList(scope, categories);
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
  public void removeProperty(PropertyScope scope, ArgMatcher name,
      Set<ArgMatcher> categories, ClusterInfo cluster) {
    proxy.removeProperty(scope, name, categories);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void archiveProcessProperties(RevId revId, ClusterInfo cluster) {
    proxy.archiveProcessProperties(revId);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void unarchiveProcessProperties(RevId revId, ClusterInfo cluster) {
    proxy.unarchiveProcessProperties(revId);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void addTag(String tag, ClusterInfo cluster) {
    proxy.addTag(tag);
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void addTags(Set<String> tags, boolean clearExisting, ClusterInfo cluster) {
    proxy.addTags(tags, clearExisting);
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
    proxy.removeTag(ArgMatchers.parse(tag));
    invoker.invokeLenient(void.class, cluster);
  }

  @Override
  public void renameTags(List<NameValuePair> tags, ClusterInfo cluster) {
    proxy.renameTags(tags);
    invoker.invokeLenient(void.class, cluster);
  }
  
  @Override
  public void archiveTags(RevId revId, ClusterInfo cluster) {
    proxy.archiveTags(revId);
    invoker.invokeLenient(void.class, cluster);    
  }
  
  @Override
  public void unarchiveTags(RevId revId, ClusterInfo cluster) {
    proxy.unarchiveTags(revId);
    invoker.invokeLenient(void.class, cluster);        
  }
}
