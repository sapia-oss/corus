package org.sapia.corus.client.facade;

public interface CorusConnector {

  public CorusConnectionContext getContext();

  /**
   * @return the {@link DeployerFacade}
   */
  public DeployerFacade getDeployerFacade();

  /**
   * @return the {@link ConfiguratorFacade}
   */
  public ConfiguratorFacade getConfigFacade();

  /**
   * @return the {@link CronFacade}
   */
  public CronFacade getCronFacade();

  /**
   * @return the {@link ProcessorFacade}
   */
  public ProcessorFacade getProcessorFacade();

  /**
   * @return the {@link PortManagementFacade}
   */
  public PortManagementFacade getPortManagementFacade();

  /**
   * @return the {@link ClusterFacade}.
   */
  public ClusterFacade getCluster();

  /**
   * @return the {@link RepoFacade}.
   */
  public RepoFacade getRepoFacade();

  /**
   * @return the {@link ShellScriptManagementFacade}.
   */
  public ShellScriptManagementFacade getScriptManagementFacade();

  /**
   * @return the {@link FileManagementFacade}.
   */
  public FileManagementFacade getFileManagementFacade();
}