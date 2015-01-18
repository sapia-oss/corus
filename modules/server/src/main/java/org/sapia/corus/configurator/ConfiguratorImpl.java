package org.sapia.corus.configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.DbModule;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.configurator.PropertyChangeEvent.Type;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.PropertyContainer;
import org.sapia.corus.core.PropertyProvider;
import org.sapia.ubik.rmi.Remote;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link Configurator} and {@link InternalConfigurator}
 * interfaces.
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = { Configurator.class })
@Remote(interfaces = Configurator.class)
public class ConfiguratorImpl extends ModuleHelper implements Configurator {

  public static final String PROP_SERVER_NAME   = "corus.server.name";

  private static final String CONFIG_CATEGORIES = "categories";
  
  @Autowired
  private PropertyProvider propertyProvider;

  @Autowired
  private DbModule db;

  @Autowired
  private EventDispatcher dispatcher;

  @Autowired
  private HttpModule httpModule;

  private DbMap<String, ConfigProperty> internalConfig;
  private PropertyStore                 processProperties, serverProperties;
  private DbMap<String, ConfigProperty> tags;
  private Map<String, PropertyStore>    processPropertiesByCategory  = new ConcurrentHashMap<>();

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
  
  void setInternalConfig(DbMap<String, ConfigProperty> internalConfig) {
    this.internalConfig = internalConfig;
  }
  
  Map<String, PropertyStore> getProcessPropertiesByCategory() {
    return processPropertiesByCategory;
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
    processProperties = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.process"));
    serverProperties  = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.server"));
    internalConfig    = db.getDbMap(String.class, ConfigProperty.class, "configurator.internal.config");
    
    ConfigProperty categoryList = internalConfig.get(CONFIG_CATEGORIES);
    if (categoryList != null) {
      String[] categoryNames = StringUtils.split(categoryList.getValue(), ",");
      for (String c : categoryNames) {
        processPropertiesByCategory.put(c.trim(), storeForCategory(c));
      }
    }
    tags              = db.getDbMap(String.class, ConfigProperty.class, "configurator.tags");
    propertyProvider.overrideInitProperties(new ConfigPropertyContainer(propertyProvider.getInitProperties()));
  }

  @Override
  public void start() {
    try {
      ConfiguratorHttpExtension extension = new ConfiguratorHttpExtension(this, super.serverContext());
      httpModule.addHttpExtension(extension);
    } catch (Exception e) {
      log.error("Could not add configurator HTTP extension", e);
    }
  }

  @Override
  public void dispose() {
  }
  
  // --------------------------------------------------------------------------
  // Property operations

  @Override
  public void addProperty(final PropertyScope scope, final String name, final String value, Set<String> categories) {
    if (scope == PropertyScope.PROCESS) {
      if (categories.isEmpty()) {
        store(PropertyScope.PROCESS).addProperty(name, value);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, null, scope, Type.ADD));
      } else {
        for (String c : categories) {
          String category = c.trim();
          if (category.length() > 0) {
            store(category, true).addProperty(name, value);
            dispatcher.dispatch(new PropertyChangeEvent(name, value, category, scope, Type.ADD));
          }
        }
      }
    } else{
      store(PropertyScope.SERVER).addProperty(name, value);
      dispatcher.dispatch(new PropertyChangeEvent(name, value, null, scope, Type.ADD));
    }
  }

  @Override
  public void addProperties(PropertyScope scope, Properties props, Set<String> categories, boolean clearExisting) {
    if (scope == PropertyScope.PROCESS) {
      if (categories.isEmpty()) {
        doAddProperties(PropertyScope.PROCESS, props, null, store(PropertyScope.PROCESS), clearExisting);
      } else {
        for (String c : categories) {
          String category = c.trim();
          if (category.length() > 0) {
            doAddProperties(PropertyScope.PROCESS, props, category, store(category, true), clearExisting);
          }
        }
      }
    } else {
      doAddProperties(PropertyScope.SERVER, props, null, store(PropertyScope.SERVER), clearExisting);
    }
  }
  
  @Override
  public String getProperty(String name, List<String> categories) {
    String value = serverProperties.getProperty(name);
    if (value == null) {
      value = processProperties.getProperty(name);
    }
    return value;
  }
  
  @Override
  public Properties getProperties(PropertyScope scope, List<String> categories) {
    if (scope == PropertyScope.PROCESS) {
      return doGetProcessProperties(categories, new PropertyAccumulator<Properties>() {
        private Properties result = new Properties();
        @Override
        public void onProperty(Property property) {
          result.put(property.getName(), property.getValue());
        }
        @Override
        public Properties getResult() {
          return result;
        }
      });
    } else {
      return store(PropertyScope.SERVER).getProperties();
    }
  }
  
  @Override
  public List<Property> getPropertiesList(PropertyScope scope, List<String> categories) {
    if (scope == PropertyScope.PROCESS) {
      return doGetProcessProperties(categories, new PropertyAccumulator<List<Property>>() {
        private List<Property> properties = new ArrayList<>();
        @Override
        public void onProperty(Property property) {
          properties.add(property);
        }
        @Override
        public List<Property> getResult() {
          Collections.sort(properties);
          return properties;
        }
      });
    } else {
      List<Property> propList = new ArrayList<>();
      Properties props = store(PropertyScope.SERVER).getProperties();
      for (String n : props.stringPropertyNames()) {
        propList.add(new Property(n, props.getProperty(n), null));
      }
      return propList;
    }
  }
  
  @Override
  public List<Property> getAllPropertiesList(PropertyScope scope) {
    if (scope == PropertyScope.PROCESS) {
      List<Property> propList = new ArrayList<>();
      fillPropertyList(propList, store(PropertyScope.PROCESS), null);
      for (String c : processPropertiesByCategory.keySet()) {
        fillPropertyList(propList, store(c, false), c);
      }
      Collections.sort(propList);
      return propList;
    } else {
      return getPropertiesList(PropertyScope.SERVER, new ArrayList<String>(0));
    }
  }
  
  @Override
  public void removeProperty(PropertyScope scope, Arg pattern, Set<Arg> categories) {
    if (scope == PropertyScope.PROCESS) {
      if (categories.isEmpty()) {
        doRemoveProperties(scope, null, store(PropertyScope.PROCESS), pattern);
      } else {
        for (Arg c : categories) {
          for (String k : processPropertiesByCategory.keySet()) {
            if (c.matches(k)) {
              PropertyStore store = processPropertiesByCategory.get(k);
              if (store != null) {
                doRemoveProperties(scope, k, store, pattern);
              }
            }
          }
        }
      }
    } else {
      doRemoveProperties(PropertyScope.SERVER, null, store(PropertyScope.SERVER), pattern);
    }
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
  public Set<Tag> getTags() {
    Iterator<String> names = tags.keys();
    Set<Tag> tags = new TreeSet<Tag>();
    while (names.hasNext()) {
      String name = names.next();
      tags.add(new Tag(name));
    }
    return tags;
  }

  @Override
  public void removeTag(String tag) {
    tags.remove(tag);
  }

  @Override
  public void removeTag(Arg tag) {
    for (Tag t : getTags()) {
      if (tag.matches(t.getValue())) {
        removeTag(t.getValue());
      }
    }
  }

  @Override
  public void addTags(Set<String> tags) {
    for (String t : tags) {
      if (t != null) {
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
  // Restricted methods (visbible for testing)

  PropertyStore store(PropertyScope scope) {
    if (scope == PropertyScope.SERVER) {
      return serverProperties;
    } else {
      return processProperties;
    }
  }
  
  synchronized PropertyStore store(String category, boolean createIfNotExists) {
    PropertyStore store = this.processPropertiesByCategory.get(category);
    if (store == null && createIfNotExists) {
      store = storeForCategory(category);
      processPropertiesByCategory.put(category, store);
      ConfigProperty categoryList = internalConfig.get(CONFIG_CATEGORIES);
      if (categoryList == null) {
        internalConfig.put(CONFIG_CATEGORIES, new ConfigProperty(CONFIG_CATEGORIES, category));
      } else {
        categoryList.setValue(categoryList.getValue() + "," + category);
        internalConfig.put(CONFIG_CATEGORIES, categoryList);
      }
    }
    return store;
  }
  
  private PropertyStore storeForCategory(String category) {
    return new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.process." + category));
  }
  
  private void doAddProperties(PropertyScope scope, Properties props, String category, PropertyStore store, boolean clearExisting) {
    if (clearExisting) {
      Properties stored = store.getProperties();
      for (String name : stored.stringPropertyNames()) {
        String value = stored.getProperty(name);
        store.removeProperty(name);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, category, scope, Type.REMOVE));
      }
    }
    for (String name : props.stringPropertyNames()) {
      String value = props.getProperty(name);
      if (value != null) {
        store.addProperty(name, value);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, category, scope, Type.ADD));
      }
    }
  }
  
  private void doRemoveProperties(PropertyScope scope, String category, PropertyStore store, Arg pattern) {
    for (String name : store.propertyNames()) {
      if (pattern.matches(name)) {
        String value = store.getProperty(name);
        store.removeProperty(name);
        dispatcher.dispatch(new PropertyChangeEvent(name, value, category, scope, Type.REMOVE));
      }
    } 
  }
  
  private <R> R doGetProcessProperties(List<String> categories, PropertyAccumulator<R> acc) {
    Map<String, Property> results = new HashMap<>();
    
    // getting global process properties
    OptionalValue<String> nullCategory = OptionalValue.of(null);
    populate(results, store(PropertyScope.PROCESS).getProperties(), nullCategory);
    
    // going through categories
    for (String c : categories) {
      PropertyStore store = processPropertiesByCategory.get(c);
      if (store != null) {
        populate(results, store.getProperties(), OptionalValue.of(c));
      }
    }
    for (String n : results.keySet()) {
      Property p = results.get(n);
      acc.onProperty(p);
    }
    
    return acc.getResult();
  }
  
  private void populate(Map<String, Property> toPopulate, Properties props, OptionalValue<String> category) {
    for (String n : props.stringPropertyNames()) {
      String value = props.getProperty(n);
      if (value != null) {
        toPopulate.put(n, new Property(n, value, category.isNull() ? null : category.get()));
      }
    }
  }

  private void fillPropertyList(List<Property> toFill, PropertyStore store, String category) {
    Properties props = store.getProperties();
    for (String n : props.stringPropertyNames()) {
      toFill.add(new Property(n, props.getProperty(n), category));
    }
  }
  
  // ==========================================================================
  // Inner classes
  
  interface PropertyAccumulator<R> {
    
    public void onProperty(Property property);
    
    public R getResult();
  }
  
  class ConfigPropertyContainer implements PropertyContainer {
    private PropertyContainer nested;

    public ConfigPropertyContainer(PropertyContainer nested) {
      this.nested = nested;
    }

    public String getProperty(String name) {
      String value = store(PropertyScope.SERVER).getProperty(name);
      if (value == null) {
        value = nested.getProperty(name);
      }
      return value;
    }
  }

}
