package org.sapia.corus.client.facade;

import java.io.File;

import org.sapia.corus.client.cli.DefaultClientFileSystem;

/**
 * Creates {@link CorusConnector} instances.
 * 
 * @author yduchesne
 *
 */
public class CorusConnectorBuilder {
  
  private String host;
  private int    port;
  private File   baseDir = new File(System.getProperty("user.dir"));
  
  private CorusConnectorBuilder() {
  }
  
  /**
   * @param host the Corus host to connect to.
   * @return this instance.
   */
  public CorusConnectorBuilder host(String host) {
    this.host = host;
    return this;
  }
  
  /**
   * @param port the Corus port to connect to.
   * @return this instance.
   */
  public CorusConnectorBuilder port(int port) {
    this.port = port;
    return this;
  }
  
  /**
   * @param baseDir the base directory relatively to which to perform operations (used by deploy).
   * @return this instance.
   */
  public CorusConnectorBuilder basedir(File baseDir) {
    this.baseDir = baseDir;
    return this;
  }
 
  /**
   * @return a new {@link CorusConnector}, based on this instance's configuration.
   */
  public CorusConnector build() {
    try {
      CorusConnectionContext context = new CorusConnectionContextImpl(host, port, new DefaultClientFileSystem(baseDir));
      CorusConnectorImpl connector = new CorusConnectorImpl(context);
      return connector;
    } catch (Exception e) {
      throw new IllegalStateException("Error caught trying to create connection", e);
    }
  }
  
  /**
   * @return a new {@link CorusConnectorBuilder} instance.
   */
  public static CorusConnectorBuilder newInstance() {
    return new CorusConnectorBuilder();
  }

}
