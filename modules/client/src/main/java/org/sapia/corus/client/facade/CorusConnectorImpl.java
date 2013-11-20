package org.sapia.corus.client.facade;

import org.sapia.corus.client.facade.impl.ClusterfacadeImpl;
import org.sapia.corus.client.facade.impl.ConfiguratorFacadeImpl;
import org.sapia.corus.client.facade.impl.CronFacadeImpl;
import org.sapia.corus.client.facade.impl.DeployerFacadeImpl;
import org.sapia.corus.client.facade.impl.FileManagementFacadeImpl;
import org.sapia.corus.client.facade.impl.PortManagementFacadeImpl;
import org.sapia.corus.client.facade.impl.ProcessorFacadeImpl;
import org.sapia.corus.client.facade.impl.RepoFacadeImpl;
import org.sapia.corus.client.facade.impl.ShellScriptManagementFacadeImpl;

/**
 * An instance of this class wraps the various facades dealing with various
 * aspects of interacting with the Corus server.
 * 
 * @author yduchesne
 * 
 */
public class CorusConnectorImpl implements CorusConnector {

  private CorusConnectionContext context;
  private DeployerFacade deployer;
  private ProcessorFacade processor;
  private CronFacade cron;
  private PortManagementFacade ports;
  private ConfiguratorFacade config;
  private ClusterFacade cluster;
  private RepoFacade repo;
  private ShellScriptManagementFacade scripts;
  private FileManagementFacade files;

  public CorusConnectorImpl(CorusConnectionContext context) {
    this.context = context;
    deployer = new DeployerFacadeImpl(context);
    processor = new ProcessorFacadeImpl(context);
    cron = new CronFacadeImpl(context);
    ports = new PortManagementFacadeImpl(context);
    config = new ConfiguratorFacadeImpl(context);
    cluster = new ClusterfacadeImpl(context);
    repo = new RepoFacadeImpl(context);
    scripts = new ShellScriptManagementFacadeImpl(context);
    files = new FileManagementFacadeImpl(context);
  }

  @Override
  public CorusConnectionContext getContext() {
    return context;
  }

  @Override
  public DeployerFacade getDeployerFacade() {
    return deployer;
  }

  @Override
  public ConfiguratorFacade getConfigFacade() {
    return config;
  }

  @Override
  public CronFacade getCronFacade() {
    return cron;
  }

  @Override
  public ProcessorFacade getProcessorFacade() {
    return processor;
  }

  @Override
  public PortManagementFacade getPortManagementFacade() {
    return ports;
  }

  @Override
  public ClusterFacade getCluster() {
    return cluster;
  }

  @Override
  public RepoFacade getRepoFacade() {
    return repo;
  }

  @Override
  public ShellScriptManagementFacade getScriptManagementFacade() {
    return scripts;
  }

  @Override
  public FileManagementFacade getFileManagementFacade() {
    return files;
  }

}
