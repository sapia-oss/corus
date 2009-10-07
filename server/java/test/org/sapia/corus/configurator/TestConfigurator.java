package org.sapia.corus.configurator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.configurator.Configurator;
import org.sapia.corus.util.NameValuePair;

public class TestConfigurator implements Configurator{

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

  public List<NameValuePair> getPropertiesAsNameValuePairs(PropertyScope scope) {
    return new ArrayList<NameValuePair>();
  }

  public String getProperty(String name) {
    return null;
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
