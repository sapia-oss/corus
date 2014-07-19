package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.transport.Connection;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.Streams;

/**
 * This class models a deployment on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class Deployment {

  static final int BUFSZ = 8192;

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private ServerContext context;
  private Connection conn;
  private DeploymentMetadata meta;

  /**
   * @param conn
   *          the {@link Connection} that represents the network link with the
   *          client that is performing the deployment.
   */
  public Deployment(ServerContext context, Connection conn) {
    this.context = context;
    this.conn = conn;
  }

  public DeploymentMetadata getMetadata() throws IOException {
    if (meta == null) {
      ObjectInputStream ois = SerializationStreams.createObjectInputStream(conn.getInputStream());
      try {
        meta = (DeploymentMetadata) ois.readObject();
      } catch (ClassNotFoundException e) {
        throw new IOException("Class not found: " + e.getMessage());
      }
    }
    return meta;
  }

  /**
   * Closes the {@link Connection} that this instance encapsulates.
   */
  public void close() {
    conn.close();
  }

  /**
   * Streams the deployment data to the passed in stream.
   * <p>
   * IMPORTANT: this method closes the given stream.
   * 
   * @param deployOutput
   *          an {@link OutputStream}.
   * @throws IOException
   *           if an IO problem occurs while performing this operation.
   */
  public void deploy(DeployOutputStream deployOutput) throws IOException {
    long length = getMetadata().getContentLength();
    InputStream is = conn.getInputStream();
    long total = 0;
    byte[] buf = new byte[BUFSZ];
    int read = 0;
    long remaining = length;

    log.debug(String.format("Processing deployment stream of %s bytes : %s", length, meta.getFileName()));
    try {
      while (remaining > 0 && (read = is.read(buf, 0, BUFSZ > remaining ? (int) remaining : BUFSZ)) > 0) {
        total = total + read;
        remaining -= read;
        deployOutput.write(buf, 0, read);
        deployOutput.flush();
      }

      if (length != total) {
        throw new IllegalStateException(String.format("Expected %s bytes, processed %s", length, total));
      }
      deployOutput.flush();
    } finally {
      try {
        deployOutput.close();
      } catch (IOException e) {
        log.warn("Error closing deployment output stream", e);
      }
      Streams.closeSilently(is);
    }
    handleResult(deployOutput.getProgressQueue());
  }

  private void handleResult(ProgressQueue result) throws IOException {
    ObjectOutputStream os = createObjectOutputStream(conn.getOutputStream());
    os.writeObject(result);
    Streams.flushAndCloseSilently(os);
  }

  // --------------------------------------------------------------------------
  // Provided for testing purposes

  ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
    ObjectOutputStream oos = MarshalStreamFactory.createOutputStream(os);
    ((RmiObjectOutput) oos).setUp(getMetadata().getOrigin(), context.getTransport().getServerAddress().getTransportType());
    return oos;
  }
}
