package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.ubik.net.ServerAddress;

/**
 * This interface specifies the behavior of client-side deployment.
 * 
 * @author yduchesne
 */
public interface DeploymentClient {
  
  /**
   * Connects to the Corus server corresponding to the given address.
   * 
   * @param addr
   *          the {@link ServerAddress} of the server.
   * @throws IOException
   *           if no connection could be made.
   */
  public void connect(ServerAddress addr) throws IOException;

  /**
   * Performs a deployment.
   * 
   * @param meta
   *          a {@link DeploymentMetadata} holding deployment information used
   *          by this instance.
   * @param is
   *          the stream of data to deploy.
   * @throws IOException
   *           if a problem occurs during deployment.
   */
  public ProgressQueue deploy(DeploymentMetadata meta, InputStream is) throws IOException;
  
  /**
   * This method returns a stream that will be used to upload deployment data.
   * 
   * @return an {@link OutputStream}.
   * @throws IOException
   *           if a problem occurs acquiring the given stream.
   */
  public OutputStream getOutputStream() throws IOException;

  /**
   * This method returns a stream that will be used to acquire the deployment
   * result.
   * 
   * @return an {@link InputStream}.
   * @throws IOException
   *           if a problem occurs acquiring the given stream.
   */
  public InputStream getInputStream() throws IOException;

  /**
   * Releases all system resources that this instance holds.
   */
  public void close();

}
