package org.sapia.corus.configurator;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.PropertyContainer;
import org.sapia.corus.core.PropertyProvider;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link Configurator} and {@link InternalConfigurator} interfaces.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface={Configurator.class})
@Remote(interfaces=Configurator.class)
public class ConfiguratorImpl extends ModuleHelper implements Configurator {
  
  public static final String PROP_SERVER_NAME = "corus.server.name";

  @Autowired
  private PropertyProvider propertyProvider;
  
  @Autowired
  private DbModule db;
 
  @Autowired
  private EventDispatcher dispatcher;
  
  private PropertyStore processProperties, serverProperties;
  private DbMap<String, ConfigProperty> tags;
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setPropertyProvider(PropertyProvider propertyProvider) {
    this.propertyProvider = propertyProvider;
  }
  
  void setDb(DbModule db) {
    this.db = db;
  }
  
  void setDispatcher(EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }
  
  void setTags(DbMap<String, ConfigProperty> tags) {
    this.tags = tags;
  }
  
  void setProcessProperties(PropertyStore processProperties) {
    this.processProperties = processProperties;
  }
  
  void setServerProperties(PropertyStore serverProperties) {
    this.serverProperties = serverProperties;
  }
  
  // --------------------------------------------------------------------------
  // Module interface impl
  
  @Override
  public String getRoleName() {
    return Configurator.ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    processProperties   = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.process"));
    serverProperties    = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.server"));
    tags                = db.getDbMap(String.class, ConfigProperty.class, "configurator.tags");
    propertyProvider.overrideInitProperties(new ConfigPropertyContainer(propertyProvider.getInitProperties()));
  }
  
  @Override
  public void dispose() {}

  // --------------------------------------------------------------------------
  // Property operations
 
  @Override
  public void addProperty(final PropertyScope scope, final String name, final String value) {
    store(scope).addProperty(name, value);
    dispatcher.dispatch(new PropertyChangeEvent(name, value, scope, Type.ADD));
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public void addProperties(PropertyScope scope, Properties props,
      boolean clearExisting) {
    PropertyStore store = store(scope);
    if(clearExisting){
      Properties stored = store.getProperties();
      for (String name : stored.stringPropertyNames()) {
        String value = stored.getProperty(name);
        store.removeProperty(name);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, scope, Type.REMOVE));
      }
    }
    Enumeration names = props.propertyNames();
    while(names.hasMoreElements()){
      String name  = (String)names.nextElement();
      String value = props.getProperty(name);
      if(value != null){
        store.addProperty(name, value);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, scope, Type.ADD));
      }
    }
  }

  @Override
  public String getProperty(String name) {
    String value = serverProperties.getProperty(name);
    if(value == null){
      value = processProperties.getProperty(name);
    }
    return value;
  }
  
  @Override
  public void removeProperty(PropertyScope scope, Arg pattern) {
    PropertyStore store = store(scope);
    Properties stored = store.getProperties();
    for (String name : stored.stringPropertyNames()) {
      if (pattern.matches(name)) {
        String value = stored.getProperty(name);
        store.removeProperty(name);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, scope, Type.REMOVE));
      }
    }
  }
  
  @Override
  public Properties getProperties(PropertyScope scope) {
    return store(scope).getProperties();
  }
  
  @Override
  public List<NameValuePair> getPropertiesAsNameValuePairs(PropertyScope scope) {
    return doGetPropertiesAsNameValuePairs(scope);
  }
  
  // --------------------------------------------------------------------------
  // Tag operation
  
  @Override
  public void addTag(String tag) {
    tags.put(tag, new ConfigProperty(tag, tag));
  }
  
  @Override
  public void clearTags() {
    tags.clear();
  }
  
  @Override
  public Set<String> getTags() {
    Iterator<String> names = tags.keys();
    Set<String> tags = new TreeSet<String>();
    while(names.hasNext()){
      String name = names.next();
      tags.add(name);
    }
    return tags;
  }
  
  @Override
  public void removeTag(String tag) {
    tags.remove(tag);
  }
  
  @Override
  public void removeTag(Arg tag) {
    for(String t:getTags()){
      if(tag.matches(t)){
        removeTag(t);
      }
    }
  }
  
  @Override
  public void addTags(Set<String> tags) {
    for(String t:tags){
      if(t != null){
        addTag(t);
      }
    }
  }
  
  @Override
  public synchronized void renameTags(List<NameValuePair> tags) {
    for (NameValuePair t : tags) {
      if (this.tags.get(t.getName()) != null) {
        this.tags.remove(t.getName());
        addTag(t.getValue());
      }
    }
  }

  // --------------------------------------------------------------------------
  // Restricted methods
  
  private PropertyStore store(PropertyScope scope){
    if(scope == PropertyScope.SERVER){
      return serverProperties;
    }
    else{
      return processProperties;
    }
  }
  
  private List<NameValuePair> doGetPropertiesAsNameValuePairs(PropertyScope scope) {
    Properties props = store(scope).getProperties();
    List<NameValuePair> toReturn = new ArrayList<NameValuePair>(props.size());
    Enumeration<?> keys = props.propertyNames();
    while(keys.hasMoreElements()){
      String key = (String)keys.nextElement();
      String value = props.getProperty(key);
      NameValuePair pair = new NameValuePair(key, value);
      toReturn.add(pair);
    }
    Collections.sort(toReturn);
    return toReturn;
  }
  
  // ==========================================================================
  // Inner class
  
  class ConfigPropertyContainer implements PropertyContainer{
    private PropertyContainer nested;
    public ConfigPropertyContainer(PropertyContainer nested) {
      this.nested = nested;
    }
    
    public String getProperty(String name) {
      String value = store(PropertyScope.SERVER).getProperty(name);
      if(value == null){
        value = nested.getProperty(name);
      }
      return value;
    }
  }

}
