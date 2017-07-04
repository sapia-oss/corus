package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.ubik.concurrent.NamedThreadFactory;

/**
 * An output stream that is used for clustered deployment.
 * 
 * @author Yanick Duchesne
 */
public class ClusteredDeployOutputStreamImpl extends DeployOutputStreamImpl {
  
  private DeployOutputStream next;
  
  private ExecutorService    executor;

  /**
   * @param destFile
   *          the {@link File} to upload to.
   * @param meta
   *          the {@link DeploymentMetadata} corresponding to the uploaded file.
   * @param handler
   *          the {@link DeploymentHandler} to notify once upload has completed.
   * @param next
   *          the "next" {@link DeployOutputStream} that is part of the
   *          deployment chain.
   * @throws FileNotFoundException
   *           if the destination file could not be opened.
   */
  ClusteredDeployOutputStreamImpl(
      File destFile, 
      DeploymentMetadata meta, 
      DeploymentHandler handler, 
      DeployOutputStream next)
      throws FileNotFoundException {
    super(destFile, meta, handler);
    this.next     = next;
    this.executor = Executors.newSingleThreadExecutor(
        NamedThreadFactory.createWith("ClusteredDeployOutputStream").setDaemon(true)
    );
  }
  
  @Override
  public void write(final byte[] bytes) throws IOException {
    Future<Void> response = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        next.write(bytes);
        return null;
      }
    });
    super.write(bytes);
    doWaitFor(response);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length) throws IOException {
    Future<Void> response = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        next.write(bytes, offset, length);
        return null;
      }
    });
    super.write(bytes, offset, length);
    doWaitFor(response);
  }

  @Override
  public void write(final int data) throws IOException {
    Future<Void> response = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        next.write(data);
        return null;
      }
    });
    super.write(data);
    doWaitFor(response);
  }
  
  @Override
  public void flush() throws IOException {
    Future<Void> response = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        next.flush();
        return null;
      }
    });
    super.flush();
    doWaitFor(response);
  }
  
  @Override
  public void close() throws IOException {
    super.flush();
    super.close();
    executor.shutdown();
    next.close();
  }
  
  private void doWaitFor(Future<Void> response) throws IOException {
    try {
      response.get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      throw new IllegalStateException("Exception occurred while streaming data", e);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted while streaming data", e);
    }
  }
}
