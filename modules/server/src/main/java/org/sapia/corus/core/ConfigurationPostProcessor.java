package org.sapia.corus.core;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.corus.client.services.configurator.Configurator;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;

/**
 * An instance of this class replaces property variables within the spring config files, using the
 * values supplied as part of the corus configuration, or the ones stored in the {@link Configurator} -
 * the latter as precedence.
 * 
 * @author yduchesne
 *
 */
class ConfigurationPostProcessor implements BeanFactoryPostProcessor{

  private PropertyProvider properties;

  ConfigurationPostProcessor(ServerContext delegate, PropertyProvider properties) {
    this.properties = properties;
  }

  
  
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
      throws BeansException {
    
    for(String name:factory.getBeanDefinitionNames()){
      BeanDefinition def = factory.getBeanDefinition(name);
      MutablePropertyValues propValues = def.getPropertyValues();
      PropertyValue[] vals = propValues.getPropertyValues();
      for(int i = 0; i < vals.length; i++){
        PropertyValue oldVal = vals[i];
        
        String toReplace = null;
        if(oldVal.getValue() instanceof TypedStringValue){
          toReplace = ((TypedStringValue)oldVal.getValue()).getValue();
        }
        else if(oldVal.getValue() instanceof String){
          toReplace = (String)oldVal.getValue();
        }
        
        if(toReplace != null){
          StrSubstitutor subs = new StrSubstitutor(new StrLookupImpl());
          PropertyValue newVal = new PropertyValue(
              oldVal.getName(), 
              subs.replace(toReplace));
          propValues.setPropertyValueAt(newVal, i);
        }
      }
    }
  }
  
  class StrLookupImpl extends StrLookup{
    
    @Override
    public String lookup(String name) {
      return properties.getInitProperties().getProperty(name);
    }
  }
}
