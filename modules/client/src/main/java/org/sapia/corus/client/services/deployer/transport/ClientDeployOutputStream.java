package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.corus.client.ClientDebug;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.ubik.serialization.SerializationStreams;

/**
 * @author Yanick Duchesne
 */
public class ClientDeployOutputStream implements DeployOutputStream {

  private static final int BYTES_FOR_INT = 4;

  private ClientDebug log = ClientDebug.get(getClass());

  private OutputStream out;
  private DeploymentClient client;
  private ProgressQueue queue;
  private boolean closed;
  private int written;
  
  public ClientDeployOutputStream(DeploymentMetadata meta, DeploymentClient client) throws IOException {
    this.client = client;
    out = this.client.getOutputStream();
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(out);
    oos.writeObject(meta);
    oos.flush();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      log.trace("Got %s bytes written", written);
      try {
        ObjectInputStream ois = SerializationStreams.createObjectInputStream(client.getInputStream());
        queue = (ProgressQueue) ois.readObject();
      } catch (ClassNotFoundException e) {
        throw new IOException("Could not deserialize response: " + e.getMessage());
      } finally {
        client.close();
      }
    }
  }
  
  @Override
  public ProgressQueue commit() throws IOException {
    close();
    if (queue == null) {
      throw new IllegalStateException("Progress not received from server");
    }
    return queue;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    written += len;
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
    written += b.length;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    written += BYTES_FOR_INT;
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }
}
