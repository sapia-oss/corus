package org.sapia.corus.deployer;

import org.sapia.corus.CorusException;
import org.sapia.corus.CorusRuntime;
import org.sapia.corus.LogicException;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.ServerStartedEvent;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.cluster.ClusterManager;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.transport.AbstractDeploymentClient;
import org.sapia.corus.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.corus.deployer.transport.DeploymentProcessor;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.corus.http.HttpModule;
import org.sapia.corus.processor.ProcessorExtension;
import org.sapia.corus.taskmanager.TaskManager;
import org.sapia.corus.util.IDGenerator;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;
import org.sapia.corus.util.ProgressQueueLogger;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.replication.ReplicationStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This component implements the <code>Deployer</code> interface.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeployerImpl extends ModuleHelper implements Deployer,
  DeploymentConnector, Interceptor {
  /**
   * The file lock timeout property name (<code>file-lock-timeout</code>).
   */
  public static final String LOCK_TIMEOUT = "file-lock-timeout";

  /**
   * The default deployment directory (<code>user.dir/deploy</code>).
   */
  public static final String DEFAULT_DEPLOY_DIR = CorusRuntime.getCorusHome() +
    java.io.File.separator + "deploy";

  /**
   * The default temp directory (<code>user.dir/tmp</code>).
   */
  public static final String DEFAULT_TMP_DIR = CorusRuntime.getCorusHome() +
    java.io.File.separator + "tmp";

  /**
   * The default file lock timeout (2 minutes).
   */
  public static final long    DEFAULT_FILELOCK_TIMEOUT = 120000;
  private String              _deployDir   = DEFAULT_DEPLOY_DIR;
  private String              _tmpDir      = DEFAULT_TMP_DIR;
  private Map                 _deployLocks = new HashMap();
  private Map                 _tmpLocks    = new HashMap();
  private DistributionStore   _store       = new DistributionStore();
  private DeploymentProcessor _processor;
  private long                _timeout     = DEFAULT_FILELOCK_TIMEOUT;

  /**
   * @param deployDir the path to the deployment directory.
   */
  public void setDeployDir(String deployDir) {
    _deployDir = deployDir;
  }

  /**
   * @param tmpDir the path to the temporary directory.
   */
  public void setTempDir(String tmpDir) {
    _tmpDir = tmpDir;
  }

  /**
   * @param timeout the amount of time (in millis) that deployed files are locked before extraction.
   */
  public void setFileLockTimeout(long timeout) {
    _timeout = timeout;
  }

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    String pattern = CorusRuntime.getCorus().getDomain() + '_' +
      ((TCPAddress) CorusRuntime.getTransport().getServerAddress()).getPort();

    if (_deployDir != null) {
      _deployDir = _deployDir + File.separator + pattern;
    } else {
      _deployDir = DEFAULT_DEPLOY_DIR + File.separator + pattern;
    }

    if (_tmpDir != null) {
      _tmpDir = _tmpDir + File.separator + pattern;
    } else {
      _tmpDir = DEFAULT_TMP_DIR + File.separator + pattern;
    }

    File f = new File(new File(_deployDir).getAbsolutePath());
    f.mkdirs();
    assertFile(f);

    f = new File(new File(_tmpDir).getAbsolutePath());
    f.mkdirs();
    assertFile(f);

    logger().debug("Deploy dir: " + f.getPath());

    logger().info("Initializing: rebuilding distribution objects");

    TaskManager tm = (TaskManager) env().lookup(TaskManager.ROLE);

    try {
      ProgressQueueLogger.transferMessages(_log,
        tm.execSyncTask("BuildDistTask", new BuildDistTask(_deployDir, _store)));
    } catch (Throwable t) {
      throw new CorusException(t);
    }

    logger().info("Distribution objects succesfully rebuilt");

    EventDispatcher disp = (EventDispatcher) CorusRuntime.getCorus().lookup(EventDispatcher.ROLE);
    disp.addInterceptor(ServerStartedEvent.class, this);
  }

  /**
   * Called when the Corus server has started.
   */
  public void onServerStartedEvent(ServerStartedEvent evt) {
    try {
      _processor = new DeploymentProcessor(this, logger());
      _processor.init();
      _processor.start();
    } catch (Exception e) {
      logger().error("Could not start deployment processor", e);
    }
    try{
      HttpModule module = (HttpModule)CorusRuntime.getCorus().lookup(HttpModule.ROLE);
      DeployerExtension ext = new DeployerExtension(this);
      module.addHttpExtension(ext);
    }catch (Exception e){
      logger().error("Could not add deployer HTTP extension", e);
    }
    
  }

  /**
   * @see org.sapia.soto.Service#dispose()
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
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return Deployer.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                    Deployer INTERFACE IMPLEMENTATION
  ////////////////////////////////////////////////////////////////////*/

  public Distribution getDistribution(CommandArg name, CommandArg version)
    throws LogicException {
    return _store.getDistribution(name, version);
  }

  public List getDistributions() {
    return _store.getDistributions();
  }

  public List getDistributions(CommandArg name) {
    return _store.getDistributions(name);
  }
  
  public List getDistributions(CommandArg name, CommandArg version) {
    return _store.getDistributions(name, version);
  }

  public ProgressQueue undeploy(CommandArg distName, CommandArg version) {
    try {
      TaskManager tm = (TaskManager) env().lookup(TaskManager.ROLE);

      return tm.execSyncTask("UndeployTask",
        new UndeployTask(_store, distName, version));
    } catch (Throwable e) {
      ProgressQueueImpl q = new ProgressQueueImpl();
      q.error(e);

      return q;
    }
  }

  /**
   * @return this instance's <code>DistributionStore</code>.
   */
  public DistributionStore getDistributionStore() {
    return _store;
  }

  /**
   * @see org.sapia.corus.deployer.transport.DeploymentConnector#connect(org.sapia.corus.deployer.transport.Deployment)
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
      Set siblings;

      try {
        siblings = ((ClusterManager) CorusRuntime.getCorus().lookup(ClusterManager.ROLE)).getHostAddresses();
      } catch (CorusException e) {
        deployment.close();
        logger().error("Could not lookup ClusterManager while performing deployment",
          e);

        return;
      }

      Set                 targets = meta.getTargets();
      Set                 visited = meta.getVisited();
      ServerAddress       addr;
      ServerAddress       current = CorusRuntime.getTransport()
                                                .getServerAddress();
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
                out = new ClusteredDeployOutputStreamImpl(_tmpDir +
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
              out = new DeployOutputStreamImpl(_tmpDir + File.separator +
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
            out = new DeployOutputStreamImpl(_tmpDir + File.separator +
                fileName, fileName, this);
          } else {
						// chaining deployment to next host.        	
            out = new ClusteredDeployOutputStreamImpl(_tmpDir + File.separator +
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
        out = new DeployOutputStreamImpl(_tmpDir + File.separator + fileName,
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

  synchronized ProgressQueue unlockDeployFile(String fileName) {
    _log.info("Finished uploading " + fileName);
    releaseFileLock(_deployLocks, fileName);

    try {
      TaskManager tm = (TaskManager) env().lookup(TaskManager.ROLE);

      return tm.execSyncTask("DeployTask",
        new DeployTask(_store, fileName, _tmpDir, _deployDir));
    } catch (Throwable e) {
      _log.error("Could not deploy", e);

      ProgressQueueImpl q = new ProgressQueueImpl();
      q.error(e);

      return q;
    }
  }

  private synchronized FileLock acquireFileLock(Map locks, String fileName)
    throws ConcurrentDeploymentException, InterruptedException {
    FileLock fLock = (FileLock) locks.get(fileName);

    if (fLock == null) {
      fLock = new FileLock(fileName, _timeout);
      locks.put(fileName, fLock);
    }

    fLock.acquire();

    return fLock;
  }

  private synchronized void releaseFileLock(Map locks, String fileName) {
    FileLock fLock = (FileLock) locks.get(fileName);

    if (fLock != null) {
      fLock.release();
    }
  }

  private void assertFile(File f) {
    f.mkdirs();

    if (!f.isDirectory()) {
      throw new IllegalArgumentException(_deployDir + " not a directory");
    }

    if (!f.exists()) {
      throw new IllegalArgumentException(_deployDir + " does not exist");
    }
  }
}
