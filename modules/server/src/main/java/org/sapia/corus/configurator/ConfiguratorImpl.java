package org.sapia.corus.configurator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.DbModule;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.configurator.PropertyChangeEvent.EventType;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.PropertyContainer;
import org.sapia.corus.core.PropertyProvider;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.springframework.beans.SimpleTypeConverter;
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
public class ConfiguratorImpl extends ModuleHelper implements InternalConfigurator {

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
  private Properties                    defaultServerProperties;
  private DbMap<String, ConfigProperty> tags;
  private Map<String, PropertyStore>    processPropertiesByCategory  = new ConcurrentHashMap<>();
  private SimpleTypeConverter           converter                    = new SimpleTypeConverter();

  
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
    defaultServerProperties = new Properties();
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/sapia/corus/default.properties");
    if (is == null) {
      throw new IllegalStateException("Could not find default properties");
    }
    defaultServerProperties.load(is);
    is.close();
    
    processProperties = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.process"));
    serverProperties  = new PropertyStore(db.getDbMap(String.class, ConfigProperty.class, "configurator.properties.server"));
    internalConfig    = db.getDbMap(String.class, ConfigProperty.class, "configurator.internal.config");
    tags              = db.getDbMap(String.class, ConfigProperty.class, "configurator.tags");
    propertyProvider.overrideInitProperties(new ConfigPropertyContainer(propertyProvider.getInitProperties()));
    
    ConfigProperty categoryList = internalConfig.get(CONFIG_CATEGORIES);
    if (categoryList != null) {
      String[] categoryNames = StringUtils.split(categoryList.getValue(), ",");
      for (String c : categoryNames) {
        processPropertiesByCategory.put(c.trim(), storeForCategory(c));
      }
    }
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
  // InternalConfigurator interface
  
  @Override
  public <T> void registerForPropertyChange(final String propertyName, final DynamicProperty<T> dynProperty) {
    dispatcher.addInterceptor(PropertyChangeEvent.class, new PropertyChangeInterceptor<T>(propertyName, dynProperty));
  }
  
  // --------------------------------------------------------------------------
  // Property operations

  @Override
  public void addProperty(final PropertyScope scope, String name, String value, Set<String> categories) {
    PropertyChangeEvent event = new PropertyChangeEvent(EventType.ADD, scope);
    
    if (PropertyScope.PROCESS == scope) {
      if (categories.isEmpty()) {
        store(PropertyScope.PROCESS).addProperty(name, value);
        event.addProperty(new Property(name, value, null));
      } else {
        for (String c : categories) {
          String category = c.trim();
          if (category.length() > 0) {
            store(category, true).addProperty(name, value);
            event.addProperty(new Property(name, value, category));
          }
        }
      }
    } else if (PropertyScope.SERVER == scope) {
      store(PropertyScope.SERVER).addProperty(name, value);
      event.addProperty(new Property(name, value, null));
    }
    
    if (event.getProperties().size() > 0) {
      dispatcher.dispatch(event);
    }
  }

  @Override
  public void addProperties(PropertyScope scope, Properties props, Set<String> categories, boolean clearExisting) {
    Set<Property> deletedProperties = new LinkedHashSet<>();
    Set<Property> addedProperties = new LinkedHashSet<>();

    // 1. Process property changes
    if (PropertyScope.PROCESS == scope) {
      if (categories.isEmpty()) {
        PropertyStore store = store(PropertyScope.PROCESS);
        if (clearExisting) {
          doRemoveProperties(scope, null, store, ArgMatchers.any(), deletedProperties);
        }
        doAddProperties(scope, props, null, store, addedProperties);
      } else {
        for (String c : categories) {
          String category = c.trim();
          if (category.length() > 0) {
            PropertyStore store = store(category, true);
            if (clearExisting) {
              doRemoveProperties(scope, category, store, ArgMatchers.any(), deletedProperties);
            }
            doAddProperties(scope, props, category, store, addedProperties);
          }
        }
      }
    } else if (PropertyScope.SERVER == scope) {
      PropertyStore store = store(PropertyScope.SERVER);
      if (clearExisting) {
        doRemoveProperties(scope, null, store, ArgMatchers.any(), deletedProperties);
      }
      doAddProperties(scope, props, null, store, addedProperties);
    }

    // 2. Consolidate deleted and added properties
    Map<NameCategoryKey, Property> deletedPropertyMap = new LinkedHashMap<>();
    for (Property prop: deletedProperties) {
      deletedPropertyMap.put(
          new NameCategoryKey(prop.getName(), prop.getCategory().isSet()? prop.getCategory().get(): null), prop);
    }
    
    for (Property prop: addedProperties) {
      Property toDel = deletedPropertyMap.get(
          new NameCategoryKey(prop.getName(), prop.getCategory().isSet()? prop.getCategory().get(): null));
      if (toDel != null) {
        deletedProperties.remove(toDel);
      }
    }
    
    // 3. Fire events
    if (deletedProperties.size() > 0) {
      dispatcher.dispatch(new PropertyChangeEvent(EventType.REMOVE, scope, deletedProperties));
    }
    if (addedProperties.size() > 0) {
      dispatcher.dispatch(new PropertyChangeEvent(EventType.ADD, scope, addedProperties));
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
    if (PropertyScope.PROCESS == scope) {
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
    if (PropertyScope.PROCESS == scope) {
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
  public List<Property> getAllPropertiesList(PropertyScope scope, Set<ArgMatcher> categories) {
    if (PropertyScope.PROCESS == scope) {
      List<Property> propList = new ArrayList<>();
      if (categories.isEmpty()) {
        fillPropertyList(propList, store(PropertyScope.PROCESS), null);
        for (String c : processPropertiesByCategory.keySet()) {
          fillPropertyList(propList, store(c, false), c);
        }
      } else {
        for (String c : processPropertiesByCategory.keySet()) {
          for (ArgMatcher matcher : categories) {
            if (matcher.matches(c)) {
               fillPropertyList(propList, store(c, false), c);
            }
          }
        }
      }
   
      Collections.sort(propList);
      return propList;
      
    } else {
      return getPropertiesList(PropertyScope.SERVER, new ArrayList<String>(0));
    }
  }
  
  @Override
  public void removeProperty(PropertyScope scope, ArgMatcher pattern, Set<ArgMatcher> categories) {
    Set<Property> deletedProperties = new LinkedHashSet<>();
    
    if (PropertyScope.PROCESS == scope) {
      if (categories.isEmpty()) {
        doRemoveProperties(scope, null, store(PropertyScope.PROCESS), pattern, deletedProperties);
      } else {
        for (ArgMatcher c : categories) {
          for (String k : processPropertiesByCategory.keySet()) {
            if (c.matches(k)) {
              PropertyStore store = processPropertiesByCategory.get(k);
              if (store != null) {
                doRemoveProperties(scope, k, store, pattern, deletedProperties);
              }
            }
          }
        }
      }
    } else {
      doRemoveProperties(PropertyScope.SERVER, null, store(PropertyScope.SERVER), pattern, deletedProperties);
    }
    
    if (deletedProperties.size() > 0) {
      dispatcher.dispatch(new PropertyChangeEvent(EventType.REMOVE, scope, deletedProperties));
    }
  }
  
  @Override
  public void archiveProcessProperties(RevId revId) {
    store(PropertyScope.PROCESS).archive(revId);
    for (Map.Entry<String, PropertyStore> store : processPropertiesByCategory.entrySet()) {
      store.getValue().archive(revId);
    }
  }
  
  @Override
  public void unarchiveProcessProperties(RevId revId) {
    store(PropertyScope.PROCESS).unarchive(revId);
    for (Map.Entry<String, PropertyStore> store : processPropertiesByCategory.entrySet()) {
      store.getValue().unarchive(revId);
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
  public void removeTag(ArgMatcher tag) {
    for (Tag t : getTags()) {
      if (tag.matches(t.getValue())) {
        removeTag(t.getValue());
      }
    }
  }

  @Override
  public void addTags(Set<String> tags, boolean clearExisting) {
    if (clearExisting) {
      this.tags.clear();
    }
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
  
  @Override
  public void archiveTags(RevId revId) {
    tags.clearArchive(revId);
    tags.archive(revId, Collects.convertAsSet(tags.values(), new Func<String, ConfigProperty>() {
      @Override
      public String call(ConfigProperty arg) {
        return arg.getValue();
      }
    }));
  }
  
  @Override
  public void unarchiveTags(RevId revId) {
    tags.unarchive(revId);
  }

  // --------------------------------------------------------------------------
  // Dumpable interface
  
  @Override
  public void dump(JsonStream stream) {
    stream.beginObject().field("properties"); // begin properties
    stream.beginObject().field("server"); // begin server
    for (Property p : getAllPropertiesList(PropertyScope.SERVER, new HashSet<ArgMatcher>())) {
      p.toJson(stream);
    }
    stream.endObject(); // end server
    
    stream.beginObject().field("process"); // begin process
    for (Property p : getAllPropertiesList(PropertyScope.PROCESS, new HashSet<ArgMatcher>())) {
      p.toJson(stream);
    }
    stream.endObject(); // end process
    stream.endObject(); // end properties
    
    // begin tags
    stream.beginObject().field("tags").strings(Collects.convertAsList(getTags(), new Func<String, Tag>() {
      @Override
      public String call(Tag t) {
        return t.getValue();
      }
    }));
    // end tags
    stream.endObject();
    
  }
  
  @Override
  public void load(JsonInput dump) {
    for (JsonInput props : dump.iterate("properties")) {
      Set<String> emptyCategories = new HashSet<String>();
      for (JsonInput prop : props.iterate("server")) {
        String name  = prop.getString("name");
        String value = prop.getString("value");
        addProperty(PropertyScope.SERVER, name, value, emptyCategories);
      }
      
      for (JsonInput prop : props.iterate("process")) {
        String name  = prop.getString("name");
        String value = prop.getString("value");
        String cat   = prop.getString("category");
        if (cat.equals("N/A")) {
          addProperty(PropertyScope.PROCESS, name, value, emptyCategories);
        } else {
          addProperty(PropertyScope.PROCESS, name, value, Collects.arrayToSet(cat));
        }
      }
    }
    addTags(Collects.arrayToSet(dump.getStringArray("tags")), false);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods (visible for testing)

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
  
  private void doAddProperties(PropertyScope scope, Properties props, String category, PropertyStore store, Set<Property> addedProperties) {
    for (String name : props.stringPropertyNames()) {
      String value = props.getProperty(name);
      if (value != null) {
        store.addProperty(name, value);
        Property added = new Property(name, value, category);
        addedProperties.add(added);
      }
    }
  }
  
  private void doRemoveProperties(PropertyScope scope, String category, PropertyStore store, ArgMatcher pattern, Set<Property> deletedProperties) {
    // Copy names to avoid concurrent modifications
    List<String> propertyNames = new ArrayList<>();
    for (String name: store.propertyNames()) {
      propertyNames.add(name);
    }
    
    for (String name : propertyNames) {
      if (pattern.matches(name)) {
        String value = store.getProperty(name);
        store.removeProperty(name);
        deletedProperties.add(new Property(name, value, category));
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
  
  public final class PropertyChangeInterceptor<T> implements Interceptor {
    private final String propertyName;
    private final DynamicProperty<T> dynProperty;

    private PropertyChangeInterceptor(String propertyName,
        DynamicProperty<T> dynProperty) {
      this.propertyName = propertyName;
      this.dynProperty = dynProperty;
    }

    public void onPropertyChangeEvent(PropertyChangeEvent event) {
      if (event.containsProperty(propertyName)) {
        if (log.isDebugEnabled()) {
          log.debug("Property change detected for " + propertyName);
        }
        Property property = event.getFirstPropertyFor(propertyName);

        // if the operation is a removal, we want to reset to the default value.
        String updatedValue = property.getValue();
        if (EventType.REMOVE == event.getEventType()) {
          String defaultProp = defaultServerProperties.getProperty(propertyName);
          if (defaultProp != null) {
            updatedValue = defaultProp;
          }
        }
        dynProperty.setValue(converter.convertIfNecessary(updatedValue, dynProperty.getType()));
      }
    }
  }

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
  
  
  
  public static class NameCategoryKey {
    public String name;
    public String category;
    public NameCategoryKey(String aName, String aCategory) {
      name = aName;
      category = aCategory;
    }
    
    @Override
    public int hashCode() {
      return ObjectUtils.safeHashCode(name, category);
    }
    
    @Override
    public boolean equals(Object other) {
      if (other instanceof NameCategoryKey) {
        return ObjectUtils.safeEquals(name, ((NameCategoryKey) other).name) 
            && ObjectUtils.safeEquals(category, ((NameCategoryKey) other).category);
        
      } else {
        return false;
      }
    }
  }

}
