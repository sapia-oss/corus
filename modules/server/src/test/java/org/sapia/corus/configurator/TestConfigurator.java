package org.sapia.corus.configurator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.PropertyMasker;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.database.RevId;

public class TestConfigurator implements Configurator {

  private Properties processProps = new Properties();
  private Properties serverProps = new Properties();

  
  public String getRoleName() {
    return Configurator.ROLE;
  }
  
  @Override
  public void addProperties(PropertyScope scope, Properties props,
      Set<String> categories, boolean clearExisting) {
    props(scope).putAll(props);
  }
  
  @Override
  public void addProperty(PropertyScope scope, String name, String value,
      Set<String> categories) {
    props(scope).setProperty(name, value);
  }
  
  @Override
  public void addTag(String tag) {
  }

  @Override
  public void addTags(Set<String> tags, boolean clearExisting) {
  }

  @Override
  public void clearTags() {
  }

  
  @Override
  public Properties getProperties(PropertyScope scope, List<String> categories) {
    return props(scope);
  }
 
  @Override
  public List<Property> getAllPropertiesList(PropertyScope scope, Set<ArgMatcher> categories) {
    Properties props = props(scope);
    List<Property> pairs = new ArrayList<Property>();
    for (String n : props.stringPropertyNames()) {
      pairs.add(new Property(n, props.getProperty(n)));
    }
    return pairs;
  }
  
  @Override
  public List<Property> getPropertiesList(PropertyScope scope,
      List<String> categories) {
    Properties props = props(scope);
    List<Property> pairs = new ArrayList<Property>();
    for (String n : props.stringPropertyNames()) {
      pairs.add(new Property(n, props.getProperty(n)));
    }
    return pairs;
  }
  
  @Override
  public String getProperty(String name, List<String> categories) {
    String prop = serverProps.getProperty(name);
    if (prop == null) {
      prop = serverProps.getProperty(name);
    }
    return prop;
  }

  @Override
  public Set<Tag> getTags() {
    return new HashSet<Tag>();
  }
  
  @Override
  public PropertyMasker getPropertyMasker() {
    return PropertyMasker.newDefaultInstance();
  }
  
  @Override
  public void removeProperty(PropertyScope scope, ArgMatcher name, Set<ArgMatcher> categories) {
  }

  @Override
  public void removeTag(ArgMatcher tag) {
  }

  @Override
  public void removeTag(String tag) {
  }
  
  @Override
  public void renameTags(List<NameValuePair> tags) {
  }
  
  @Override
  public void archiveProcessProperties(RevId revId) {
  }
  
  @Override
  public void unarchiveProcessProperties(RevId revId) {
  }
  
  @Override
  public void archiveTags(RevId revId) {
  }
  
  @Override
  public void unarchiveTags(RevId revId) {
  }
  
  @Override
  public void dump(JsonStream stream) {
  }
  
  @Override
  public void load(JsonInput dump) {
  }
  
  private Properties props(PropertyScope scope) {
    if (scope == PropertyScope.SERVER) {
      return serverProps;
    }
    return processProps;
  }

}
