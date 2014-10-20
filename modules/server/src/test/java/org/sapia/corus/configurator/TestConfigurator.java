package org.sapia.corus.configurator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Tag;

public class TestConfigurator implements Configurator {

  private Properties processProps = new Properties();
  private Properties serverProps = new Properties();

  
  public String getRoleName() {
    return Configurator.ROLE;
  }

  @Override
  public void addProperty(PropertyScope scope, String name, String value) {
    props(scope).setProperty(name, value);
  }
  
  @Override
  public void addProperties(PropertyScope scope, Properties props,
      boolean clearExisting) {
    props(scope).putAll(props);
  }
  
  @Override
  public void addTag(String tag) {
  }

  @Override
  public void addTags(Set<String> tags) {
  }

  @Override
  public void clearTags() {
  }

  @Override
  public Properties getProperties(PropertyScope scope) {
    return props(scope);
  }
  
  @Override
  public List<NameValuePair> getPropertiesAsNameValuePairs(PropertyScope scope) {
    Properties props = props(scope);
    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    for (String n : props.stringPropertyNames()) {
      pairs.add(new NameValuePair(n, props.getProperty(n)));
    }
    return pairs;
  }

  @Override
  public String getProperty(String name) {
    return getProperty(name);
  }

  @Override
  public Set<Tag> getTags() {
    return new HashSet<Tag>();
  }

  @Override
  public void removeProperty(PropertyScope scope, Arg name) {
  }

  @Override
  public void removeTag(Arg tag) {
  }

  @Override
  public void removeTag(String tag) {
  }
  
  @Override
  public void renameTags(List<NameValuePair> tags) {

  }
  
  private Properties props(PropertyScope scope) {
    if (scope == PropertyScope.SERVER) {
      return serverProps;
    }
    return processProps;
  }

}
