package org.sapia.corus;

import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.corus.processor.ExecConfigDatabase;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.task.ProcessorTaskStrategy;
import org.sapia.corus.taskmanager.core.TaskManager;

/**
 * An instance of this class holds internal services.
 * 
 * @author yduchesne
 *
 */
public class InternalServiceContext {
  
  private Map<String, Object> services = new HashMap<String, Object>();
  
  /**
   * @return the {@link EventDispatcher}
   */
  public EventDispatcher getEventDispatcher(){
    return lookup(EventDispatcher.class);
  }
  
  /**
   * @return the {@link DistributionDatabase}
   */
  public DistributionDatabase getDistributions(){
    return lookup(DistributionDatabase.class);
  }
  
  /**
   * @return the {@link ProcessRepository}
   */
  public ProcessRepository getProcesses(){
    return lookup(ProcessRepository.class);
  }
  
  /**
   * @return the {@link ExecConfigDatabase}
   */
  public ExecConfigDatabase getExecConfigs(){
    return lookup(ExecConfigDatabase.class);
  }
  
  /**
   * @return the {@link Processor}
   */
  public Processor getProcessor(){
    return lookup(Processor.class);
  }
  
  /**
   * @return the {@link Deployer}
   */
  public Deployer getDeployer(){
    return lookup(Deployer.class);
  }
  
  /**
   * @return the {@link TaskManager}
   */
  public TaskManager getTaskManager(){
    return lookup(TaskManager.class);
  }
  
  /**
   * @return the {@link ProcessorTaskStrategy}
   */
  public ProcessorTaskStrategy getProcessorTaskStrategy(){
    return lookup(ProcessorTaskStrategy.class);
  }
  
  /**
   * Returns the service instance corresponding to the given interface.
   * 
   * @param <S>
   * @param serviceInterface the interface of the desired service.
   * @return the desired service.
   * @throws IllegalStateException if the service is not found.
   */
  public <S> S lookup(Class<S> serviceInterface){
    Object service = services.get(serviceInterface.getName());
    if(service == null){
      throw new IllegalStateException("No internal service found for: " + serviceInterface);
    }
    return serviceInterface.cast(service);
  }
  
  /**
   * Binds the given service instance "under" the given interface.
   * @param serviceInterface the interface to use to internally bind the service (this
   * interface can later be used for lookup).
   * 
   * @param service an {@link Object} corresponding to the service to bind.
   */
  public void bind(Class<?> serviceInterface, Object service){
    bind(serviceInterface.getName(), service);
  }
  
  void bind(String interfaceName, Object service){
    if(services.get(interfaceName) != null){
      throw new IllegalStateException("Internal service already found for: " + interfaceName);
    }
    services.put(interfaceName, service);
  }

}
