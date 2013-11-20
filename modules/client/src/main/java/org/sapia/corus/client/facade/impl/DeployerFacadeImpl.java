package org.sapia.corus.client.facade.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DeployerFacade;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeployOsAdapter;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.FileDeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.ShellScriptDeploymentMetadata;
import org.sapia.ubik.util.Streams;

public class DeployerFacadeImpl extends FacadeHelper<Deployer> implements DeployerFacade {

  /**
   * Interface meant for internal use.
   * 
   */
  interface MetadataFactory {

    /**
     * @param fileName
     *          the name of the file that will be uploaded.
     * @param fileLen
     *          the length of the file (in bytes).
     * @param cluster
     *          the {@link ClusterInfo} providing information about the
     *          operation's clustering.
     * @return the {@link DeploymentMetadata} corresponding to the file to
     *         upload.
     */
    public DeploymentMetadata create(String fileName, long fileLen, ClusterInfo cluster);

  }

  // --------------------------------------------------------------------------

  private static final int BUFSZ = 2048;

  public DeployerFacadeImpl(CorusConnectionContext context) {
    super(context, Deployer.class);
  }

  @Override
  public synchronized ProgressQueue deployDistribution(final String fileName, final ClusterInfo cluster) throws IOException,
      ConcurrentDeploymentException, DuplicateDistributionException, Exception {
    return doDeployArtifact(fileName, cluster, new MetadataFactory() {
      @Override
      public DeploymentMetadata create(String fileName, long fileLen, ClusterInfo cluster) {
        return new DistributionDeploymentMetadata(fileName, fileLen, cluster);
      }
    });
  }

  @Override
  public ProgressQueue deployFile(String fileName, final String destinationDir, ClusterInfo cluster) throws IOException, Exception {
    return doDeployArtifact(fileName, cluster, new MetadataFactory() {
      @Override
      public DeploymentMetadata create(String fileName, long fileLen, ClusterInfo cluster) {
        return new FileDeploymentMetadata(fileName, fileLen, destinationDir, cluster);
      }
    });
  }

  @Override
  public ProgressQueue deployScript(String scriptFileName, final String alias, final String description, final ClusterInfo cluster)
      throws IOException, Exception {
    return doDeployArtifact(scriptFileName, cluster, new MetadataFactory() {
      @Override
      public DeploymentMetadata create(String fileName, long fileLen, ClusterInfo cluster) {
        return new ShellScriptDeploymentMetadata(fileName, fileLen, alias, description, cluster);
      }
    });
  }

  private ProgressQueue doDeployArtifact(final String fileName, final ClusterInfo cluster, final MetadataFactory factory) throws IOException,
      ConcurrentDeploymentException, DuplicateDistributionException, Exception {

    final ProgressQueueImpl queue = new ProgressQueueImpl();

    if (ArgFactory.isPattern(fileName)) {
      Thread deployer = new Thread(new Runnable() {
        public void run() {
          try {
            Object[] baseDirAndFilePattern = split(context.getFileSystem(), fileName);
            File baseDir = (File) baseDirAndFilePattern[0];
            Arg pattern = ArgFactory.parse((String) baseDirAndFilePattern[1]);
            File[] files = baseDir.listFiles();
            ProgressQueue tmp = null;
            int fileCount = 0;
            for (int i = 0; i < files.length; i++) {
              if (!files[i].isDirectory() && pattern.matches(files[i].getName())) {
                queue.info("Deploying: " + files[i].getName());
                try {
                  fileCount++;
                  DeploymentMetadata meta = factory.create(files[i].getName(), files[i].length(), cluster);
                  tmp = doDeploy(files[i], meta, cluster);
                  while (tmp.hasNext()) {
                    List<ProgressMsg> lst = tmp.fetchNext();
                    for (int j = 0; j < lst.size(); j++) {
                      ProgressMsg msg = (ProgressMsg) lst.get(j);
                      queue.addMsg(msg);
                    }
                  }
                } catch (Exception e) {
                  queue.error(e);
                }
              }
            }
            if (fileCount == 0) {
              queue.warning("No file found to deploy for: " + fileName);
            } else {
              queue.info("Batch deployment completed");
            }
          } finally {
            queue.close();
          }
        }
      });
      deployer.start();
      return queue;
    } else {
      File toDeploy = context.getFileSystem().getFile(fileName);
      DeploymentMetadata meta = factory.create(toDeploy.getName(), toDeploy.length(), cluster);
      return doDeploy(toDeploy, meta, cluster);
    }

  }

  private ProgressQueue doDeploy(File toDeploy, DeploymentMetadata meta, ClusterInfo cluster) throws IOException, ConcurrentDeploymentException,
      DuplicateDistributionException, Exception {

    OutputStream os = null;
    BufferedInputStream bis = null;
    try {
      if (!toDeploy.exists()) {
        throw new IOException(toDeploy.getAbsolutePath() + " does not exist");
      }

      if (toDeploy.isDirectory()) {
        throw new IOException(toDeploy.getAbsolutePath() + " is a directory");
      }

      meta.getClusterInfo().getTargets().addAll(cluster.getTargets());
      DeployOutputStream dos = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(context.getAddress()));

      os = new DeployOsAdapter(dos);
      bis = new BufferedInputStream(new FileInputStream(toDeploy));

      byte[] b = new byte[BUFSZ];
      int read;
      while ((read = bis.read(b)) > -1) {
        os.write(b, 0, read);
      }
      return dos.getProgressQueue();
    } finally {
      Streams.flushAndCloseSilently(os);
      Streams.closeSilently(bis);
    }
  }

  @Override
  public synchronized ProgressQueue undeployDistribution(DistributionCriteria criteria, ClusterInfo cluster) throws RunningProcessesException {
    proxy.undeploy(criteria);
    try {
      return invoker.invoke(ProgressQueue.class, cluster);
    } catch (RunningProcessesException e) {
      throw (RunningProcessesException) e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized Results<List<Distribution>> getDistributions(DistributionCriteria criteria, ClusterInfo cluster) {
    Results<List<Distribution>> results = new Results<List<Distribution>>();
    proxy.getDistributions(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  static Object[] split(ClientFileSystem fileSys, String fileName) {
    if (fileName.startsWith(ArgFactory.PATTERN)) {
      return new Object[] { fileSys.getBaseDir(), fileName };
    } else {
      String baseDirName = fileName.substring(0, fileName.indexOf(ArgFactory.PATTERN));
      int idx;
      if ((idx = baseDirName.lastIndexOf("/")) > 0 || (idx = baseDirName.lastIndexOf("\\")) > 0) {
        baseDirName = fileName.substring(0, idx);
        idx = idx + 1;
      } else {
        idx = fileName.indexOf(ArgFactory.PATTERN);
      }
      File baseDir = fileSys.getFile(baseDirName);

      if (baseDir.exists()) {
        String pattern = fileName.substring(idx);
        return new Object[] { baseDir, pattern };
      } else {
        String pattern = fileName.substring(idx);
        return new Object[] { fileSys.getBaseDir(), pattern };
      }

    }
  }

}
