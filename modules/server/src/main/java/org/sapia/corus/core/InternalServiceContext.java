package org.sapia.corus.core;

import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.processor.ExecConfigDatabase;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.TaskManager;

/**
 * An instance of this class holds "internal" services (i.e.: services are
 * singletion beans that implement behavior that is internal to the Corus
 * server).
 * <p>
 * Services that are bound to this context are not necessarily and exclusively
 * the ones configured as Spring beans. Indeed, these so-called services are
 * bound to this instance using one of the {@link #bind(Class, Object)} of this
 * class.
 * <p>
 * The {@link ModuleLifeCycleManager} automatically binds to this context those
 * beans that are annotated with {@link Bind}. Binding can occur at anytime, but
 * it is typically done at initialization/start time.
 * <p>
 * Any object that has access to an instance of this class can also bind its own
 * objects (i.e.: those that it creates itself and are not instantiated by the
 * Spring container).
 * 
 * 
 * @author yduchesne
 * 
 */
public class InternalServiceContext {

  private Map<String, Object> services = new HashMap<String, Object>();

  /**
   * @return the {@link EventDispatcher}
   */
  public EventDispatcher getEventDispatcher() {
    return lookup(EventDispatcher.class);
  }

  /**
   * @return the {@link ClusterManager}.
   */
  public ClusterManager getClusterManager() {
    return lookup(ClusterManager.class);
  }

  /**
   * @return the {@link DistributionDatabase}
   */
  public DistributionDatabase getDistributions() {
    return lookup(DistributionDatabase.class);
  }

  /**
   * @return the {@link ProcessRepository}
   */
  public ProcessRepository getProcesses() {
    return lookup(ProcessRepository.class);
  }

  /**
   * @return the {@link ExecConfigDatabase}
   */
  public ExecConfigDatabase getExecConfigs() {
    return lookup(ExecConfigDatabase.class);
  }

  /**
   * @return the {@link Processor}
   */
  public Processor getProcessor() {
    return lookup(Processor.class);
  }

  /**
   * @return the {@link Deployer}
   */
  public Deployer getDeployer() {
    return lookup(Deployer.class);
  }

  /**
   * @return the {@link TaskManager}
   */
  public TaskManager getTaskManager() {
    return lookup(TaskManager.class);
  }

  /**
   * @return the {@link PortManager}
   */
  public PortManager getPortManager() {
    return lookup(PortManager.class);
  }

  /**
   * @return the {@link Configurator}
   */
  public Configurator getConfigurator() {
    return lookup(Configurator.class);
  }

  /**
   * @return the {@link FileSystemModule}.
   */
  public FileSystemModule getFileSystem() {
    return lookup(FileSystemModule.class);
  }

  /**
   * @return the {@link FileManager}.
   */
  public FileManager getFileManager() {
    return lookup(FileManager.class);
  }

  /**
   * @return the {@link ShellScriptManager}.
   */
  public ShellScriptManager getScriptManager() {
    return lookup(ShellScriptManager.class);
  }

  /**
   * @return the {@link OsModule}.
   */
  public OsModule getOS() {
    return lookup(OsModule.class);
  }

  /**
   * Returns the service instance corresponding to the given interface.
   * 
   * @param <S>
   * @param serviceInterface
   *          the interface of the desired service.
   * @return the desired service.
   * @throws IllegalStateException
   *           if the service is not found.
   */
  public <S> S lookup(Class<S> serviceInterface) {
    Object service = services.get(serviceInterface.getName());
    if (service == null) {
      throw new IllegalStateException(String.format("No internal service found for: %s", serviceInterface));
    }
    return serviceInterface.cast(service);
  }

  /**
   * Looks up the service with the given name and returns it.
   * 
   * @param name
   *          the name of the service to return.
   * @return an {@link Object} matching the given name.
   */
  public Object lookup(String name) {
    Object service = services.get(name);
    if (service == null) {
      throw new IllegalStateException(String.format("No internal service found for: %s", name));
    }
    return service;
  }

  /**
   * Binds the given service instance "under" the given interface.
   * 
   * @param serviceInterface
   *          the interface to use to internally bind the service (this
   *          interface can later be used for lookup).
   * @param service
   *          an {@link Object} corresponding to the service to bind.
   * @throws IllegalStateException
   *           if a service is already bound for the given name.
   */
  public void bind(Class<?> serviceInterface, Object service) {
    doBind(serviceInterface.getName(), service, false);
  }

  /**
   * Binds the given service instance "under" the given interface - ignoring any
   * already existing binding for that interface.
   * 
   * @param serviceInterface
   *          the interface to use to internally bind the service (this
   *          interface can later be used for lookup).
   * @param service
   *          an {@link Object} corresponding to the service to bind.
   */
  public void rebind(Class<?> serviceInterface, Object service) {
    doBind(serviceInterface.getName(), service, true);
  }

  private void doBind(String interfaceName, Object service, boolean rebind) {
    if (services.get(interfaceName) != null && !rebind) {
      throw new IllegalArgumentException(String.format("Internal service already found for: %s", interfaceName));
    }
    services.put(interfaceName, service);
  }
}
