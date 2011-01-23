package org.sapia.corus.client.facade.impl;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;

public class ConfiguratorFacadeImpl 
  extends FacadeHelper<Configurator> implements ConfiguratorFacade{
  
  public ConfiguratorFacadeImpl(CorusConnectionContext context) {
    super(context, Configurator.class);
  }
  
  @Override
  public Results<List<NameValuePair>> getProperties(PropertyScope scope,
      ClusterInfo cluster) {
    Results<List<NameValuePair>>  results = new Results<List<NameValuePair>>();
    proxy.getPropertiesAsNameValuePairs(scope);
    invoker.invokeLenient(results, cluster);
    return results;   
  }
  
  @Override
  public Results<Set<String>> getTags(ClusterInfo cluster) {
    Results<Set<String>>  results = new Results<Set<String>>();
    proxy.getTags();
    invoker.invokeLenient(results, cluster);
    return results;   
  }
  
  @Override
  public void addProperty(PropertyScope scope, String name, String value,
      ClusterInfo cluster) {
    proxy.addProperty(scope, name, value);
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
  public void removeProperty(PropertyScope scope, String name, ClusterInfo cluster) {
    proxy.removeProperty(scope, ArgFactory.parse(name));
    invoker.invokeLenient(void.class, cluster); 
  }
  
  @Override
  public void removeTag(String tag, ClusterInfo cluster) {
    proxy.removeTag(ArgFactory.parse(tag));
    invoker.invokeLenient(void.class, cluster); 
  }
}
