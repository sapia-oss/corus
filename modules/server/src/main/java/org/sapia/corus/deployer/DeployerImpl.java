package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.core.IORuntimeException;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RollbackScriptNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteringHelper;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.ServerStartedEvent;
import org.sapia.corus.deployer.task.BuildDistTask;
import org.sapia.corus.deployer.task.RollbackTask;
import org.sapia.corus.deployer.task.UnarchiveAndDeployTask;
import org.sapia.corus.deployer.task.UndeployAndArchiveTask;
import org.sapia.corus.deployer.task.UndeployTask;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.corus.deployer.transport.DeploymentProcessor;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This component implements the {@link Deployer} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = { Deployer.class, InternalDeployer.class })
@Remote(interfaces = { Deployer.class })
public class DeployerImpl extends ModuleHelper implements InternalDeployer, DeploymentConnector, Interceptor {
  
  private static final int DEFAULT_THROTTLE = 1;
  
  /**
   * The file lock timeout property name (<code>file-lock-timeout</code>).
   */
  public static final String LOCK_TIMEOUT = "file-lock-timeout";

  @Autowired
  private EventDispatcher events;

  @Autowired
  private HttpModule http;

  @Autowired
  private TaskManager taskman;

  @Autowired
  private ClusterManager cluster;

  @Autowired
  private DeployerConfiguration configuration;

  private List<DeploymentHandler> deploymentHandlers = new ArrayList<DeploymentHandler>();

  private DeploymentProcessor processor;
  private DistributionDatabase store;

  /**
   * Returns this instance's {@link DeployerConfiguration}
   */
  public DeployerConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * @param deploymentHandlers
   *          a {@link List} of {@link DeploymentHandler}s to assign.
   */
  public void setDeploymentHandlers(List<DeploymentHandler> deploymentHandlers) {
    this.deploymentHandlers = deploymentHandlers;
  }

  /**
   * @see Service#init()
   */
  @Override
  public void init() throws Exception {

    store = new DistributionDatabaseImpl();

    services().bind(DistributionDatabase.class, store);

    services().getTaskManager().registerThrottle(DeployerThrottleKeys.DEPLOY_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(DEFAULT_THROTTLE));
    services().getTaskManager().registerThrottle(DeployerThrottleKeys.UNDEPLOY_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(DEFAULT_THROTTLE));
    services().getTaskManager().registerThrottle(DeployerThrottleKeys.ROLLBACK_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(DEFAULT_THROTTLE));
    services().getTaskManager().registerThrottle(DeployerThrottleKeys.UNARCHIVE_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(DEFAULT_THROTTLE));
    services().getTaskManager().registerThrottle(DeployerThrottleKeys.DEPLOY_UNARCHIVED_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(DEFAULT_THROTTLE));
    
    String defaultDeployDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("deploy").createFilePath();

    String defaultRepoDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("files").addDir("repo").createFilePath();

    String defaultTmpDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("tmp").createFilePath();

    String defaultScriptDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("files").addDir("scripts").createFilePath();

    String defaultUploadDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("files").addDir("uploads").createFilePath();
    
    String defaultArchiveDir = FilePath.newInstance().addDir(serverContext().getHomeDir()).addDir("archive").createFilePath();

    String pattern = serverContext().getNodeSubdirName();

    DeployerConfigurationImpl config = new DeployerConfigurationImpl();
    config.setFileLockTimeout(configuration.getFileLockTimeout());

    if (configuration.getDeployDir() != null) {
      config.setDeployDir(FilePath.newInstance().addDir(configuration.getDeployDir()).addDir(pattern).createFilePath());
    } else {
      config.setDeployDir(FilePath.newInstance().addDir(defaultDeployDir).addDir(pattern).createFilePath());
    }

    if (configuration.getTempDir() != null) {
      config.setTempDir(FilePath.newInstance().addDir(configuration.getTempDir()).addDir(pattern).createFilePath());
    } else {
      config.setTempDir(FilePath.newInstance().addDir(defaultTmpDir).addDir(pattern).createFilePath());
    }

    if (configuration.getRepoDir() != null) {
      config.setRepoDir(FilePath.newInstance().addDir(configuration.getRepoDir()).addDir(pattern).createFilePath());
    } else {
      config.setRepoDir(FilePath.newInstance().addDir(defaultRepoDir).addDir(pattern).createFilePath());
    }

    if (configuration.getScriptDir() != null) {
      config.setScriptDir(FilePath.newInstance().addDir(configuration.getScriptDir()).addDir(pattern).createFilePath());
    } else {
      config.setScriptDir(FilePath.newInstance().addDir(defaultScriptDir).addDir(pattern).createFilePath());
    }

    if (configuration.getUploadDir() != null) {
      config.setUploadDir(FilePath.newInstance().addDir(configuration.getUploadDir()).addDir(pattern).createFilePath());
    } else {
      config.setUploadDir(FilePath.newInstance().addDir(defaultUploadDir).addDir(pattern).createFilePath());
    }
    
    if (configuration.getArchiveDir() != null) {
      config.setArchiveDir(FilePath.newInstance().addDir(configuration.getArchiveDir()).addDir(pattern).createFilePath());
    } else {
      config.setArchiveDir(FilePath.newInstance().addDir(defaultArchiveDir).addDir(pattern).createFilePath());
    }

    configuration.copyFrom(config);

    File f = new File(configuration.getDeployDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Deploy dir: %s", f.getAbsolutePath()));

    f = new File(configuration.getTempDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Temporary dir: %s", f.getAbsolutePath()));
    File[] tmpFiles = f.listFiles();
    if (tmpFiles != null) {
      for (File tmp : tmpFiles) {
        log.debug(String.format("Deleting tmp file: %s", tmp.getName()));
        tmp.delete();
      }
    }

    f = new File(configuration.getRepoDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Repo dir: %s", f.getAbsolutePath()));

    f = new File(configuration.getScriptDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Script dir: %s", f.getAbsolutePath()));

    f = new File(configuration.getUploadDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Upload dir: %s", f.getAbsolutePath()));

    f = new File(configuration.getArchiveDir());
    f.mkdirs();
    assertFile(f);
    log.debug(String.format("Archive dir: %s", f.getAbsolutePath()));
    
    log.info("Initializing: rebuilding distribution objects");

    taskman.executeAndWait(new BuildDistTask(configuration.getDeployDir(), getDistributionStore()), null);

    log.info("Distribution objects succesfully rebuilt");

    events.addInterceptor(ServerStartedEvent.class, this);
  }

  /**
   * Called when the Corus server has started.
   */
  public void onServerStartedEvent(ServerStartedEvent evt) {
    try {
      processor = new DeploymentProcessor(this, serverContext());
      processor.init();
      processor.start();
    } catch (Exception e) {
      log.error("Could not start deployment processor", e);
    }
    try {
      DeployerExtension ext = new DeployerExtension(this, serverContext);
      http.addHttpExtension(ext);
    } catch (Exception e) {
      log.error("Could not add deployer HTTP extension", e);
    }
  }

  /**
   * @see Service#dispose()
   */
  public void dispose() {
    if (processor != null) {
      processor.dispose();
    }
  }

  /*
   * //////////////////////////////////////////////////////////////////// Module
   * INTERFACE IMPLEMENTATION
   * ////////////////////////////////////////////////////////////////////
   */

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return Deployer.ROLE;
  }

  /*
   * ////////////////////////////////////////////////////////////////////
   * InternalDeployer INTERFACE IMPLEMENTATION
   * ////////////////////////////////////////////////////////////////////
   */
  @Override
  public Distribution getDistribution(DistributionCriteria criteria) throws DistributionNotFoundException {
    return getDistributionStore().getDistribution(criteria);
  }

  @Override
  public List<Distribution> getDistributions(DistributionCriteria criteria) {
    List<Distribution> dists = getDistributionStore().getDistributions(criteria);
    Collections.sort(dists);
    return dists;
  }

  @Override
  public ProgressQueue undeploy(DistributionCriteria criteria, UndeployPreferences prefs) throws RunningProcessesException {
    ProcessCriteria processCriteria = ProcessCriteria.builder().distribution(criteria.getName()).version(criteria.getVersion()).build();
    if (lookup(Processor.class).getProcesses(processCriteria).size() > 0) {
      throw new RunningProcessesException("Processes for selected configuration are currently running; kill them prior to undeploying");
    }
    ProgressQueueImpl progress = new ProgressQueueImpl();
    try {
      TaskConfig cfg = TaskConfig.create(new TaskLogProgressQueue(progress));
      if (prefs.getRevId().isSet()) {
        taskman.executeAndWait(new UndeployAndArchiveTask(), TaskParams.createFor(criteria, prefs.getRevId().get()), cfg).get();
      } else {
        taskman.executeAndWait(new UndeployTask(), TaskParams.createFor(criteria), cfg).get();
      }
    } catch (Throwable e) {
      progress.error(e);
    }
    return progress;
  }

  @Override
  public synchronized File getDistributionFile(String name, String version) throws DistributionNotFoundException {
    String distFileName = name + "-" + version + ".zip";
    File distFile = FilePath.newInstance().addDir(configuration.getRepoDir()).setRelativeFile(distFileName).createFile();
    if (distFile.exists()) {
      return distFile;
    } else {
      File distDir = FilePath.newInstance().addDir(getConfiguration().getDeployDir()).addDir(name).addDir(version).addDir("common").createFile();
      if (distDir.exists()) {
        try {
          serverContext().getServices().getFileSystem().zipDirectory(distDir, true, distFile);
          return distFile;
        } catch (IOException e) {
          throw new IORuntimeException(String.format("Could generate zip file for distribution %s, %s"), e);
        }
      } else {
        throw new DistributionNotFoundException(String.format("No distribution directory found for: %s, %s", name, version));
      }
    }
  }
  
  @Override
  public ProgressQueue rollbackDistribution(String name, String version)
      throws RollbackScriptNotFoundException, DistributionNotFoundException {
    Distribution dist = getDistributionStore().getDistribution(DistributionCriteria.builder().name(name).version(version).build());
    
    ProgressQueueImpl progress = new ProgressQueueImpl();
    try {
      TaskConfig cfg = TaskConfig.create(new TaskLogProgressQueue(progress));
      taskman.executeAndWait(new RollbackTask(), dist, cfg).get();
    } catch (InvocationTargetException e) {
      progress.error(e.getCause());
    } catch (Exception e) {
      progress.error(e);
    }
    return progress;    
  }
 
  @Override
  public ProgressQueue unarchiveDistributions(RevId revId) {
    ProgressQueueImpl progress = new ProgressQueueImpl();
    try {
      TaskConfig cfg = TaskConfig.create(new TaskLogProgressQueue(progress));
      taskman.executeAndWait(new UnarchiveAndDeployTask(), TaskParams.createFor(revId) , cfg).get();
    } catch (InvocationTargetException e) {
      progress.error(e.getCause());
    } catch (Exception e) {
      progress.error(e);
    }
    return progress;        
  }
  
  /**
   * @return this instance's <code>DistributionStore</code>.
   */
  public DistributionDatabase getDistributionStore() {
    return store;
  }

  /**
   * @see DeploymentConnector#connect(Deployment)
   */
  public void connect(Deployment deployment) {
    DeploymentMetadata meta;

    try {
      meta = deployment.getMetadata();
    } catch (IOException e) {
      deployment.close();
      log.error("Could not acquire deployment metadata", e);

      return;
    }
    
    DeploymentHandler handler = selectDeploymentHandler(meta);
    File destFile = handler.getDestFile(meta);

    log.info("Processing incoming deployment: " + meta.getFileName() + " with " + handler);
    log.info("Transferring deployment stream to: " + destFile.getAbsolutePath());

    DeployOutputStream out;

    // if deployment is clustered...
    if (meta.isClustered()) {
      Set<ServerAddress> siblings;

      try {
        siblings = cluster.getHostAddresses();
      } catch (RuntimeException e) {
        deployment.close();
        log.error("Could not lookup ClusterManager while performing deployment", e);
        return;
      }

      Set<ServerAddress> visited = meta.getVisited();
      ServerAddress addr;
      ServerAddress current = serverContext().getCorusHost().getEndpoint().getServerAddress();

      log.debug(String.format("Targeted hosts: %s", meta.getClusterInfo().getTargets()));
      log.debug(String.format("Visited hosts: %s", meta.getVisited()));
      log.debug(String.format("Current host: %s", current));

      // adding this host to visited set
      visited.add(current);

      try {

        // no next host to deploy to; we have reached end of chain
        // - deployment stops here
        if ((addr = ClusteringHelper.selectNextTarget(visited, siblings)) == null) {
          if (!meta.isTargeted(current)) {
            log.info("This host is not targeted and there are not more hosts to visit. Deployment is deemed finished");
            out = new NullDeployOutputStream();
          } else {
            out = new DeployOutputStreamImpl(destFile, meta, handler);
          }
        } else {
          // chaining deployment to next host.
          if (!meta.isTargeted(current)) {
            log.info("This host is not targeted. Deployment is cascaded to the next host");
            out = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(addr));
          } else {
            log.info(String.format("Deploying to this host, and cascading deployment to the next host: %s", addr));
            DeployOutputStream next = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(addr));
            out = new ClusteredDeployOutputStreamImpl(destFile, meta, handler, next);
          }
        }
      } catch (IOException e) {
        deployment.close();
        log.error("Could not create output stream while performing clustered deployment", e);

        return;
      }
    }
    // deployment is not clustered
    else {
      try {
        out = new DeployOutputStreamImpl(destFile, meta, handler);
      } catch (FileNotFoundException e) {
        deployment.close();
        log.error("Could not create output stream while performing deployment", e);

        return;
      }
    }

    try {
      log.debug(String.format("Starting deployment of %s", meta.getFileName()));
      deployment.deploy(out);
      log.debug(String.format("Finished deployment of %s",  meta.getFileName()));
    } catch (Exception e) {
      try {
        out.close();
      } catch (IOException e2) {
        // noop
      }

      log.error("Problem deploying: " + meta.getFileName(), e);

      return;
    } finally {
      deployment.close();
    }

    log.info(String.format("Deployment upload completed for: %s", meta.getFileName()));
  }

  private void assertFile(File f) {
    f.mkdirs();

    if (!f.isDirectory()) {
      throw new IllegalArgumentException(f.getAbsolutePath() + " not a directory");
    }

    if (!f.exists()) {
      throw new IllegalArgumentException(f.getAbsolutePath() + " does not exist");
    }
  }

  private DeploymentHandler selectDeploymentHandler(DeploymentMetadata meta) {
    for (DeploymentHandler handler : deploymentHandlers) {
      if (handler.accepts(meta)) {
        return handler;
      }
    }
    throw new IllegalStateException("Could not find deployment handler for: " + meta);
  }
}
