package org.sapia.corus.core;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.annotations.Bind;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A {@link BeanPostProcessor} that processes the {@link Bind} annotation: it binds
 * the annotated bean as modules to the {@link InternalServiceContext}.
 * 
 * @author yduchesne
 */
public class ModulePostProcessor implements BeanPostProcessor{
  
  private Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getSimpleName());
  @Autowired
  private ServerContext context;
  
  @Override
  public Object postProcessAfterInitialization(Object bean, String name)
      throws BeansException {
    return bean;
  }
  
  @Override
  public Object postProcessBeforeInitialization(Object bean, String name)
      throws BeansException {
    if(bean.getClass().isAnnotationPresent(Bind.class)){
      Bind bind = bean.getClass().getAnnotation(Bind.class);
      for(Class<?> moduleInterface : bind.moduleInterface()){
        logger.debug(String.format("Binding %s as module %s", bean.getClass().getName(), moduleInterface.getName()));
        context.getServices().bind(moduleInterface, bean);
      }
    }    
    return bean;
  }

}
