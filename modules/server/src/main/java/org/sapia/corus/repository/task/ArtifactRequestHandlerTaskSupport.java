package org.sapia.corus.repository.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeployOsAdapter;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * This task provides the basic behavior for deploying artifacts to a provided
 * list of nodes.
 * 
 * @author yduchesne
 * 
 */
public abstract class ArtifactRequestHandlerTaskSupport extends RunnableTask {

  private static final int BUFSZ = 2048;

  private List<Endpoint> targets;
  private File artifactFile;
  private Func<DeploymentMetadata, Boolean> metadataFunc;

  protected ArtifactRequestHandlerTaskSupport(File artifactFile, List<Endpoint> targets, Func<DeploymentMetadata, Boolean> metadataFunc) {
    this.artifactFile = artifactFile;
    this.targets = targets;
    this.metadataFunc = metadataFunc;
  }

  public void run() {
    if (targets.isEmpty()) {
      context().debug("No targets to deploy to");
    } else {
      try {
        context().debug("Deploying to: " + targets);
        doDeploy();
      } catch (Exception e) {
        context().error("Problem performing deployment", e);
      }
    }
  }

  // --------------------------------------------------------------------------
  // Restricted visibility methods - for unit testing

  void doDeploy() throws IOException {
    OutputStream os = null;
    BufferedInputStream bis = null;
    boolean closed = false;

    List<Endpoint> targetsCopy = new ArrayList<Endpoint>(targets);

    try {

      DeploymentMetadata meta = metadataFunc.call(Boolean.TRUE);

      Endpoint first = targetsCopy.get(0);

      meta.getClusterInfo().getTargets().addAll(Collects.convertAsSet(targetsCopy, new Func<ServerAddress, Endpoint>() {
        public ServerAddress call(Endpoint arg) {
          return arg.getServerAddress();
        }
      }));

      DeployOutputStream dos = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(first.getServerAddress()));

      os = new DeployOsAdapter(dos);
      bis = new BufferedInputStream(new FileInputStream(artifactFile));

      byte[] b = new byte[BUFSZ];
      int read;

      while ((read = bis.read(b)) > -1) {
        os.write(b, 0, read);
      }

      os.flush();
      os.close();
      closed = true;

    } finally {
      if ((os != null) && !closed) {
        try {
          os.close();
        } catch (IOException e) {
        }
      }

      if (bis != null) {
        try {
          bis.close();
        } catch (IOException e) {
        }
      }
    }
  }

}
