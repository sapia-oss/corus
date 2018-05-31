package org.sapia.corus.deployer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.transport.DeploymentClient;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.Func;

/**
 * An instance of this class gracefully handles errors occurring when deployment to a remote
 * fails. Upon such an error occurring, the instance notifies the {@link DeploymentErrorCallback} 
 * passed to it. The callback abstracts the logic that potentially proceeds to complete the 
 * deployment in an alternate manner.
 * 
 * @author yduchesne
 *
 */
public class ResilientDeploymentClient implements DeploymentClient {
  
  /**
   * Called to obtain the {@link DeploymentClient} connected to the node to deploy to.
   */
  public interface DeploymentClientSupplier {
    
    public DeploymentClient getClient() throws IOException;
    
  }
  
  // ==========================================================================

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(ResilientDeploymentClient.class.getName());
  
  private DeploymentClientSupplier             clientStreamSupplier;
  private Func<List<ProgressMsg>, IOException> errorCallback;
  private DeploymentClient                     delegate;
  
  public ResilientDeploymentClient(DeploymentClientSupplier clientStreamSupplier, Func<List<ProgressMsg>, IOException> errorCallback) {
    this.clientStreamSupplier = clientStreamSupplier;
    this.errorCallback        = errorCallback;
  }
  
  @Override
  public void connect(ServerAddress addr) throws IOException {
    try {
      delegate().connect(addr);
    } catch (IOException e) {
      notifyClientError(e);
    }
  }
  
  @Override
  public ProgressQueue deploy(DeploymentMetadata meta, InputStream is) throws IOException {
    try {
      return delegate().deploy(meta, is);
    } catch (IOException e) {
      notifyClientError(e);
      return delegate().deploy(meta, is);
    }
  }
  
  @Override
  public void close() {
    try {
      delegate().close();
    } catch (IOException e) {
      // noop
    }
  }
  
  @Override
  public InputStream getInputStream() throws IOException {
    try {
      return delegate().getInputStream();
    } catch (IOException e) {
      return delegate().getInputStream();
    }
  }
  
  @Override
  public OutputStream getOutputStream() throws IOException {
    try { 
      return new ResilientOutputStream(delegate().getOutputStream());
    } catch (IOException ioe) {
      notifyClientError(ioe);
      return delegate().getOutputStream();
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private DeploymentClient delegate()  throws IOException {
    if (delegate == null) {
      try {
        delegate = clientStreamSupplier.getClient();
      } catch (IOException ioe) {
        notifyClientError(ioe);
      }
    }
    return delegate;
  }
  
  private void notifyClientError(IOException ioe) {
    
    if (delegate == null || !(delegate instanceof ErrorDeploymentClient)) {
      try {
        delegate.close();    // TODO Auto-generated method stub
      } catch (Exception err) {
        // noop
      }
      log.error("Could not open client stream: notifying so that deployment to remaining hosts may still occur", ioe); 
      ProgressQueueImpl errorQueue = new ProgressQueueImpl();
      for (ProgressMsg msg : errorCallback.call(ioe)) {
        errorQueue.addMsg(new ProgressMsg(msg.getMessage(), ProgressMsg.WARNING));
      }
      errorQueue.close();
      delegate = new ErrorDeploymentClient(errorQueue);
    }
  }

  // ==========================================================================
  // Inner classes
  
  class ResilientOutputStream extends OutputStream {
   
    private OutputStream delegateStream;
    
    public ResilientOutputStream(OutputStream delegateStream) {
      this.delegateStream = delegateStream;
    }
    
    @Override
    public void write(byte[] b) throws IOException {
      try {
        delegateStream.write(b);
      } catch (IOException e) {
        notifyStreamError(e);
      }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      try {
        delegateStream.write(b, off, len);
      } catch (IOException e) {
        notifyStreamError(e);
      }
    }
    
    @Override
    public void write(int b) throws IOException {
      try {
        delegateStream.write(b);
      } catch (IOException e) {
        notifyStreamError(e);
      }
    }
    
    @Override
    public void flush() throws IOException {
      try {
        delegateStream.flush();
      } catch (IOException e) {
        notifyStreamError(e);
      }
    }
    
    @Override
    public void close() throws IOException {
      try {
        delegateStream.close();
      } catch (IOException e) {
        notifyStreamError(e);
      }
    }
    
    private void notifyStreamError(IOException ioe) {
      if (!(delegateStream instanceof NullOutputStream)) {
        log.error("Could not open client stream: notifying so that deployment to remaining hosts may still occur", ioe); 
        try {
          delegateStream.close();    // TODO Auto-generated method stub
        } catch (Exception err) {
          // noop
        }
        delegateStream = new NullOutputStream();
        notifyClientError(ioe);
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  static class NullOutputStream extends OutputStream {
    
    @Override
    public void write(int b) throws IOException {
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }
    
    @Override
    public void write(byte[] b) throws IOException {
    }
    
    @Override
    public void flush() throws IOException {
    }
    
    @Override
    public void close() throws IOException {
    }
  }
  
  // --------------------------------------------------------------------------
  
  static class ErrorDeploymentClient implements DeploymentClient {
    
    private ProgressQueue errQueue;
    
    public ErrorDeploymentClient(ProgressQueue errQueue) {
      this.errQueue = errQueue;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public void connect(ServerAddress addr) throws IOException {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(bos)) {
        oos.writeObject(errQueue);
        oos.flush();
        oos.close();
      }
      return new ByteArrayInputStream(bos.toByteArray());
    }
    
    @Override
    public OutputStream getOutputStream() throws IOException {
      return new NullOutputStream();
    }
    
    @Override
    public ProgressQueue deploy(DeploymentMetadata meta, InputStream is) throws IOException {
      return errQueue;
    }
    
  }
  
}
