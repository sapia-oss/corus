package org.sapia.corus.configurator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.InternalConfigurator;

public class TestConfigurator implements Configurator, InternalConfigurator{

  public String getRoleName() {
    return Configurator.ROLE;
  }

  public void addProperty(PropertyScope scope, String name, String value) {
  }

  public void addTag(String tag) {
  }

  public void addTags(Set<String> tags) {
  }

  public void clearTags() {
  }

  public Properties getProperties(PropertyScope scope) {
    return new Properties();
  }
  
  @Override
  public Properties getInternalProperties(PropertyScope scope) {
    return getProperties(scope);
  }

  public List<NameValuePair> getPropertiesAsNameValuePairs(PropertyScope scope) {
    return new ArrayList<NameValuePair>();
  }
  
  @Override
  public List<NameValuePair> getInternalPropertiesAsNameValuePairs(
      PropertyScope scope) {
    return getPropertiesAsNameValuePairs(scope);
  }

  public String getProperty(String name) {
    return null;
  }
  
  @Override
  public String getInternalProperty(String name) {
    return getProperty(name);
  }

  public Set<String> getTags() {
    return new HashSet<String>();
  }

  public void removeProperty(PropertyScope scope, Arg name) {
  }

  public void removeTag(Arg tag) {
  }

  public void removeTag(String tag) {
  }
}
