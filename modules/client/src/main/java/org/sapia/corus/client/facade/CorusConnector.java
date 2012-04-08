package org.sapia.corus.client.facade;

import org.sapia.corus.client.facade.impl.ConfiguratorFacadeImpl;
import org.sapia.corus.client.facade.impl.CronFacadeImpl;
import org.sapia.corus.client.facade.impl.DeployerFacadeImpl;
import org.sapia.corus.client.facade.impl.PortManagementFacadeImpl;
import org.sapia.corus.client.facade.impl.ProcessorFacadeImpl;

/**
 * An instance of this class wraps the various facades dealing with
 * various aspects of interacting with the Corus server.
 * 
 * @author yduchesne
 *
 */
public class CorusConnector {
  
  private CorusConnectionContext context;
  private DeployerFacade 				 deployer;
  private ProcessorFacade 			 processor;
  private CronFacade 						 cron;
  private PortManagementFacade 	 ports;
  private ConfiguratorFacade 		 config;
  
  public CorusConnector(CorusConnectionContext context){
    this.context = context;
    deployer 		 = new DeployerFacadeImpl(context);
    processor 	 = new ProcessorFacadeImpl(context);
    cron 				 = new CronFacadeImpl(context);
    ports 			 = new PortManagementFacadeImpl(context);
    config 			 = new ConfiguratorFacadeImpl(context);
  }
  
  public CorusConnectionContext getContext() {
    return context;
  }
  
  /**
   * @return the {@link DeployerFacade}
   */
  public DeployerFacade getDeployerFacade(){
    return deployer;
  }
  
  /**
   * @return the {@link ConfiguratorFacade}
   */
  public ConfiguratorFacade getConfigFacade() {
    return config;
  }
  
  /**
   * @return the {@link CronFacade}
   */
  public CronFacade getCronFacade() {
    return cron;
  }
  
  /**
   * @return the {@link ProcessorFacade}
   */
  public ProcessorFacade getProcessorFacade() {
    return processor;
  }
  
  /**
   * @return the {@link PortManagementFacade}
   */
  public PortManagementFacade getPortManagementFacade() {
    return ports;
  }

}
