package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient;
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
import org.sapia.corus.deployer.task.DeployTask;
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
@Bind(moduleInterface=Deployer.class)
@Remote(interfaces=Deployer.class)
public class DeployerImpl extends ModuleHelper implements Deployer,
  DeploymentConnector, Interceptor {
  /**
   * The file lock timeout property name (<code>file-lock-timeout</code>).
   */
  public static final String LOCK_TIMEOUT = "file-lock-timeout";

  @Autowired
  private EventDispatcher _events;
  
  @Autowired
  private HttpModule _http;
  
  @Autowired
  private TaskManager _taskman;
    
  @Autowired
  private ClusterManager _cluster;
  
  @Autowired
  private DeployerConfiguration _configuration;
  
  private DeploymentProcessor _processor;
  private DistributionDatabase   _store;

  
  /**
   * Returns this instance's {@link DeployerConfiguration}
   */
  public DeployerConfiguration getConfiguration() {
    return _configuration;
  }
  

  /**
   * @see Service#init()
   */
  @Override
  public void init() throws Exception {
    
    _store = new DistributionDatabaseImpl();
    
    services().bind(DistributionDatabase.class, _store);
    
    services().getTaskManager().registerThrottle(
        DeployerThrottleKeys.DEPLOY_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(1)
    );
    services().getTaskManager().registerThrottle(
        DeployerThrottleKeys.UNDEPLOY_DISTRIBUTION, 
        ThrottleFactory.createMaxConcurrentThrottle(1)
    );

    String defaultDeployDir = serverContext().getHomeDir() + java.io.File.separator + "deploy";
    
    String defaultTmpDir = serverContext().getHomeDir() + java.io.File.separator + "tmp";
    
    String pattern = serverContext().getDomain() + '_' + serverContext().getServerAddress().getPort();
    
    DeployerConfigurationImpl config = new DeployerConfigurationImpl();
    config.setFileLockTimeout(_configuration.getFileLockTimeout());
    
    if (_configuration.getDeployDir() != null) {
      config.setDeployDir(_configuration.getDeployDir() + File.separator + pattern);
    } else {
      config.setDeployDir(defaultDeployDir + File.separator + pattern);
    }

    if (_configuration.getTempDir() != null) {
      config.setTempDir(_configuration.getTempDir() + File.separator + pattern);
    } else {
      config.setTempDir(defaultTmpDir + File.separator + pattern);
    }
    _configuration = config;
    
    File f = new File(new File(_configuration.getDeployDir()).getAbsolutePath());
    f.mkdirs();
    assertFile(f);
    logger().debug(String.format("Deploy dir: %s", f.getAbsolutePath()));
    
    f = new File(new File(_configuration.getTempDir()).getAbsolutePath());
    f.mkdirs();
    assertFile(f);
    logger().debug(String.format("Temporary dir: %s", f.getPath()));
    
    logger().info("Initializing: rebuilding distribution objects");

    _taskman.executeAndWait(new BuildDistTask(_configuration.getDeployDir(), getDistributionStore()), null);

    logger().info("Distribution objects succesfully rebuilt");

    _events.addInterceptor(ServerStartedEvent.class, this);
  }

  /**
   * Called when the Corus server has started.
   */
  public void onServerStartedEvent(ServerStartedEvent evt) {
    try {
      _processor = new DeploymentProcessor(this, serverContext(), logger());
      _processor.init();
      _processor.start();
    } catch (Exception e) {
      logger().error("Could not start deployment processor", e);
    }
    try{
      DeployerExtension ext = new DeployerExtension(this, serverContext);
      _http.addHttpExtension(ext);
    }catch (Exception e){
      logger().error("Could not add deployer HTTP extension", e);
    }
    
  }

  /**
   * @see Service#dispose()
   */
  public void dispose() {
    if (_processor != null) {
      _processor.dispose();
    }
  }

  /*////////////////////////////////////////////////////////////////////
                    Module INTERFACE IMPLEMENTATION
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return Deployer.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                    Deployer INTERFACE IMPLEMENTATION
  ////////////////////////////////////////////////////////////////////*/

  public Distribution getDistribution(DistributionCriteria criteria) 
    throws DistributionNotFoundException {
    return getDistributionStore().getDistribution(criteria);
  }
  
  public List<Distribution> getDistributions(DistributionCriteria criteria) {
    List<Distribution> dists = getDistributionStore().getDistributions(criteria);
    Collections.sort(dists);
    return dists;
  }

  public ProgressQueue undeploy(DistributionCriteria criteria) {
    ProgressQueueImpl progress = new ProgressQueueImpl();
    try {
      ProcessCriteria processCriteria = ProcessCriteria.builder()
        .distribution(criteria.getName())
        .version(criteria.getVersion())
        .build();
      if(lookup(Processor.class).getProcesses(processCriteria).size() > 0){
        throw new RunningProcessesException("Processes for selected configuration are currently running; kill them prior to undeploying");
      }

      TaskConfig cfg = TaskConfig.create(new TaskLogProgressQueue(progress));
      _taskman.executeAndWait(
          new UndeployTask(), 
          TaskParams.createFor(criteria.getName(), criteria.getVersion()), 
          cfg
      ).get();
    } catch (Throwable e) {
      progress.error(e);
    }
    return progress;
  }

  /**
   * @return this instance's <code>DistributionStore</code>.
   */
  public DistributionDatabase getDistributionStore() {
    return _store;
  }

  /**
   * @see DeploymentConnector#connect(Deployment)
   */
  public void connect(Deployment deployment) {
    String             fileName;
    DeploymentMetadata meta;

    try {
      meta       = deployment.getMetadata();
      fileName   = meta.getFileName() + "." + IDGenerator.makeId();
    } catch (IOException e) {
      deployment.close();
      logger().error("Could not acquire deployment metadata", e);

      return;
    }

    logger().info("Processing incoming deployment: " + fileName);

    DeployOutputStream out;

    // if deployment is clustered...
    if (meta.isClustered()) {
      Set<ServerAddress> siblings;

      try {
        siblings = _cluster.getHostAddresses();
      } catch (RuntimeException e) {
        deployment.close();
        logger().error("Could not lookup ClusterManager while performing deployment",
          e);

        return;
      }

      Set<ServerAddress>  targets = meta.getTargets();
      Set<ServerAddress>  visited = meta.getVisited();
      ServerAddress       addr;
      ServerAddress       current = serverContext().getServerAddress();
      ReplicationStrategy strat   = new ReplicationStrategy(visited, targets,
          siblings);
			
      // adding this host to visited set
      visited.add(current);

      // if targets have been specified...
      if (targets != null) {
        // check that this host is in targets
        if (targets.contains(current)) {
          try {
            // remove this host from targets and add it to visited set						
            targets.remove(current);

            // if there are remaining targets, chain deployment to the 
            // next one
            if ((targets.size() > 0) && (siblings.size() > 0)) {
              addr = strat.selectNextSibling();

              try {
                out = new ClusteredDeployOutputStreamImpl(_configuration.getTempDir() +
                    File.separator + fileName, fileName, this,
                    new ClientDeployOutputStream(meta,
                      DeploymentClientFactory.newDeploymentClientFor(addr)));
              } catch (IOException e) {
                deployment.close();
                logger().error("Could not create clustered output stream while performing targeted deployment", e);

                return;
              }
            }
            // no remaining targets; deployment chain stops at this 
            // host
            else {
              out = new DeployOutputStreamImpl(_configuration.getTempDir() + File.separator +
                  fileName, fileName, this);
            }
          } catch (FileNotFoundException e) {
            deployment.close();
            logger().error("Could not create output stream while performing targeted deployment", e);

            return;
          }
        }
        // this host not in targets; so jump to the next host right away
        else {
          addr = strat.selectNextSibling();

          try {
            AbstractDeploymentClient client = (AbstractDeploymentClient) DeploymentClientFactory.newDeploymentClientFor(addr);
            out = client.getDeployOutputStream(meta);
          } catch (IOException e) {
            deployment.close();
            logger().error("Could not deploy to host: " + addr + " while performing targeted deployment", e);

            return;
          }
        }
      }
      // clustered deployment with no targets specified: all hosts
      // are thus part of the deployment.
      else {
        try {
					// no next host to deploy to; we have reached end of chain  
  				// - deployment stops here        	
          if ((addr = strat.selectNextSibling()) == null) {
            out = new DeployOutputStreamImpl(_configuration.getTempDir() + File.separator +
                fileName, fileName, this);
          } else {
						// chaining deployment to next host.        	
            out = new ClusteredDeployOutputStreamImpl(_configuration.getTempDir() + File.separator +
                fileName, fileName, this,
                new ClientDeployOutputStream(meta,
                  DeploymentClientFactory.newDeploymentClientFor(addr)));
          }
        } catch (IOException e) {
          deployment.close();
          logger().error("Could not create output stream while performing clustered deployment", e);

          return;
        }
      }
    }
    // deployment is not clustered		
    else {
      try {
        out = new DeployOutputStreamImpl(_configuration.getTempDir() + File.separator + fileName,
            fileName, this);
      } catch (FileNotFoundException e) {
        deployment.close();
        logger().error("Could not create output stream while performing deployment", e);

        return;
      }
    }

    try {
      deployment.deploy(out);
    } catch (IOException e) {
      try {
        out.close();
      } catch (IOException e2) {
        // noop
      }

      logger().error("Problem deploying: " + fileName, e);

      return;
    } finally {
      deployment.close();
    }

    logger().info("Deployment upload completed for: " + fileName);
  }


  synchronized ProgressQueue completeDeployment(String fileName) {
    log.info("Finished uploading " + fileName);
    ProgressQueue progress = new ProgressQueueImpl();
    try {
      _taskman.executeAndWait(
        new DeployTask(),
        fileName,
        TaskConfig.create(new TaskLogProgressQueue(progress))
      ).get();
      
    } catch (Throwable e) {
      log.error("Could not deploy", e);
      progress.error(e);
    }
    return progress;
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
}
