package org.sapia.corus.repository.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.exceptions.core.IORuntimeException;
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
import org.sapia.ubik.util.Streams;

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
  private Func<DeployOutputStream, PairTuple<DeploymentMetadata, ServerAddress>> deployOutputStreamFunc = 
      new Func<DeployOutputStream, PairTuple<DeploymentMetadata, ServerAddress>>() {
        @Override
        public DeployOutputStream call(PairTuple<DeploymentMetadata, ServerAddress> targetInfo) {
          try {
            return new ClientDeployOutputStream(targetInfo.getLeft(), DeploymentClientFactory.newDeploymentClientFor(targetInfo.getRight()));
          } catch (IOException e) {
            throw new IORuntimeException("Could not create deployment stream", e);
          }
        }
      };
  
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
        e.printStackTrace();
        context().error("Problem performing deployment", e);
      }
    }
  }

  // --------------------------------------------------------------------------
  // Visible for testing
  
  public void setDeployOutputStreamFunc(Func<DeployOutputStream, PairTuple<DeploymentMetadata, ServerAddress>> func) {
    this.deployOutputStreamFunc = func;
  }

  void doDeploy() throws IOException {
    OutputStream os = null;
    BufferedInputStream bis = null;
    List<Endpoint> targetsCopy = new ArrayList<Endpoint>(targets);

    try {

      DeploymentMetadata meta = metadataFunc.call(Boolean.TRUE);

      Endpoint first = targetsCopy.get(0);
      context().debug("Streaming deployment to first target in cascade: " + first.getServerAddress());
      
      // making sure were not going to deploy in a circular manner: 'this' host 
      // initiates the deployment, so we want to make sure it's not receiving the 
      // same artifacts from the last node in the chain
      meta.getVisited().add(context().getServerContext().getCorusHost().getEndpoint().getServerAddress());
      meta.getClusterInfo().getTargets().addAll(Collects.convertAsSet(targetsCopy, new Func<ServerAddress, Endpoint>() {
        public ServerAddress call(Endpoint arg) {
          return arg.getServerAddress();
        }
      }));
   
      DeployOutputStream dos = deployOutputStreamFunc.call(new PairTuple<DeploymentMetadata, ServerAddress>(meta, first.getServerAddress()));

      os  = new DeployOsAdapter(dos);
      bis = new BufferedInputStream(new FileInputStream(artifactFile));

      byte[] b = new byte[BUFSZ];
      int read;

      while ((read = bis.read(b)) > -1) {
        os.write(b, 0, read);
      }
      
      // consuming progress queue to ensure synchronous behavior when
      // chaining sequential deployment tasks
      ProgressQueue progress = dos.commit();
      while (progress.hasNext()) {
        List<ProgressMsg> messages = progress.fetchNext();
        for (ProgressMsg m : messages) {
          if (m.isThrowable()) {
            throw new IOException("Error caught while streaming deployment", m.getThrowable());
          } else if (m.isError()) {
            throw new IOException("Error caught while streaming deployment: " + m.getMessage().toString());
          }
          if (m.getStatus() == ProgressMsg.VERBOSE || m.getStatus() == ProgressMsg.DEBUG) {
            context().debug(m.getMessage().toString());
          } else if (m.getStatus() == ProgressMsg.INFO || m.getStatus() == ProgressMsg.WARNING) {
            context().info(m.getMessage().toString());
          }
        }
      }
      context().debug("Completed deployment streaming to: " + first);
    } finally {
      Streams.closeSilently(os);
      Streams.closeSilently(bis);
    }
  }
}
