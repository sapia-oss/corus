package org.sapia.corus.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.CorusServer;
import org.sapia.ubik.util.Localhost;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.springframework.beans.factory.DisposableBean;

/**
 * Implements the {@link SslExporter} interface.  
 * 
 * @author yduchesne
 *
 */
public class SslExporterBean implements SslExporter, DisposableBean {
  
  private static final int SSL_PORT_OFFSET = 443;

  private Logger          log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private ContainerServer server;
  private Connection      socketConnection;
  private String          keystorePassword;
  private String          keyPassword;
  private File            keyStoreFile;
  private int             sslPort;
  private boolean         sslEnabled;
  
  public void setKeyPassword(String keyPassword) {
    this.keyPassword = keyPassword;
  }
  
  public void setKeystorePassword(String keystorePassword) {
    this.keystorePassword = keystorePassword;
  }
  
  public void setKeystoreFile(String keystoreFileName) {
    this.keyStoreFile = new File(keystoreFileName);
  }

  public void setSslPort(int sslPort) {
    this.sslPort = sslPort;
  }
  
  public void setSslEnabled(boolean sslEnabled) {
    this.sslEnabled = sslEnabled;
  }
  
  @Override
  public void export(Container toExport) throws Exception {
    if (sslEnabled) {
      doExport(toExport);
    } else {
      log.warn("SSL is disabled");
    }
  }
  
  private void doExport(Container toExport) throws Exception {
    
    if (!keyStoreFile.exists()) {
      throw new IllegalStateException("Keystore file does not exist: " + keyStoreFile.getAbsolutePath());
    }
    
    log.info("SSL keystore file: " + keyStoreFile.getAbsolutePath());
    
    SSLContext        context = SSLContext.getInstance("TLS");
    KeyManagerFactory kmf     = KeyManagerFactory.getInstance("SunX509");
    KeyStore          ks      = KeyStore.getInstance("JKS");

    try (FileInputStream input = new FileInputStream(keyStoreFile)) {
      ks.load(input, keystorePassword.toCharArray());
    }
    
    kmf.init(ks, keyPassword.toCharArray());
    context.init(kmf.getKeyManagers(), null, null);
    
    server              = new ContainerServer(toExport);
    socketConnection    = new SocketConnection(server);
    String sslPortValue = System.getProperty(CorusConsts.PROPERTY_CORUS_PORT, "" + CorusServer.DEFAULT_PORT);
    
    if (sslPort <= 0) {
      try {
        sslPort = Integer.parseInt(sslPortValue) + SSL_PORT_OFFSET;
      } catch (NumberFormatException e) {
        throw new IllegalStateException("Invalid SSL port: " + sslPortValue);
      }
    }
    InetSocketAddress addr = new InetSocketAddress(Localhost.getPreferredLocalAddress(), sslPort);
    socketConnection.connect(addr, context);
    log.info("Exported server as SSL endpoint: " + addr);
  }
  
  @Override
  public void destroy() {
    if (socketConnection != null) {
      try {
        socketConnection.close();
      } catch (IOException e) {
        // NOOP
      }
    }
  }
  
}
