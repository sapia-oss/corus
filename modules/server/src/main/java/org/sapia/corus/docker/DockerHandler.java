package org.sapia.corus.docker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.encryption.DecryptionContext;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.services.docker.DockerHandlerClient;
import org.sapia.corus.client.services.docker.DockerRequestSupport;
import org.sapia.corus.client.services.docker.DockerSaveRequest;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.http.HttpConnection;
import org.sapia.corus.util.LoggerLogCallback;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.Handler;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.sapia.ubik.serialization.SerializationStreams;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 * Implements a custom {@link Handler} for handling Docker-specific requests.
 * 
 * @author yduchesne
 *
 */
public class DockerHandler implements Handler {
  
  private static final int BUFSZ = 8092;
  
  private Logger                log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private ServerContext         serverContext;
  private Auditor               auditor;
  private SpotifyDockerFacade   dockerFacade;

  
  public DockerHandler(
      ServerContext serverContext, 
      SpotifyDockerFacade dockerFacade) {
    this.serverContext = serverContext;
    this.auditor       = serverContext.getServices().getAuditor();
    this.dockerFacade  = dockerFacade;
    HttpTransportProvider transportProvider = (HttpTransportProvider) serverContext.getTransport().getTransportProvider();
    transportProvider.getRouter().addHandler(DockerHandlerClient.DOCKER_CONTEXT, this);
  }

  // --------------------------------------------------------------------------
  // Handler interface

  @Override
  public void shutdown() {
    // noop
  }
  
  @Override
  public void handle(Request req, Response res) {
    log.debug("Received Docker-target request");
    try {
      doHandle(req, res);
    } catch (IllegalArgumentException e) {
      log.error("Invalid parameter error caught while handling Docker request", e);
      res.setStatus(Status.NOT_ACCEPTABLE);
      res.setDescription(e.getMessage());
    } catch (Exception e) {
      log.error("Got error while handling Docker request", e);
      res.setStatus(Status.INTERNAL_SERVER_ERROR);
      res.setDescription(e.getMessage());
    } finally {
      try {
        res.close();
      } catch (IOException e) {
        // noop
      }
    }
  } 
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private void doHandle(Request req, Response res) throws Exception {
    HttpConnection conn = new HttpConnection(req, res);
    ObjectInputStream ois = SerializationStreams.createObjectInputStream(conn.getInputStream());
    DockerRequestSupport dockerRequest = (DockerRequestSupport) ois.readObject();
    conn.getInputStream().close();
    if (dockerRequest instanceof DockerSaveRequest) {
      log.debug(String.format("Handling Docker-targeted request: %s", dockerRequest));
      handleDockerSaveRequest((DockerSaveRequest) dockerRequest, req, res);
    } else {
      throw new IllegalArgumentException("Request type not handled: " + dockerRequest.getClass().getName());
    }
  }
  
  private void handleDockerSaveRequest(DockerSaveRequest dockerRequest, Request req, Response res) throws Exception {
    DecryptionContext dc        = Encryption.getDefaultDecryptionContext(serverContext.getKeyPair().getPrivate());
    AuditInfo         decrypted = dockerRequest.getAuditInfo().decryptWith(dc);
    auditor.audit(
        decrypted, 
        new RemoteAddress(req.getClientAddress().getHostString()), 
        getClass().getName(), 
        "saveImageRequest"
    );
    res.setContentType("application/octet-stream");
    log.debug(String.format("Returning Docker image payload for: %s", dockerRequest.getImageName()));
    try (InputStream imagePayload = dockerFacade.getDockerClient().saveImage(dockerRequest.getImageName(), new LoggerLogCallback(log))) {;
      try (OutputStream responseStream = res.getOutputStream()) {
        IOUtil.transfer(imagePayload, responseStream, BUFSZ);
        responseStream.flush();
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Inner classes
  
  @SuppressWarnings("serial")
  private static class RemoteAddress implements ServerAddress {
    private static final String DOCKER_TRANSPORT_TYPE = "docker";
    private String remoteAddress;
    
    private RemoteAddress(String remoteAddress) {
      this.remoteAddress = remoteAddress;
    }
    @Override
    public String getTransportType() {
      return DOCKER_TRANSPORT_TYPE;
    }
    @Override
    public String toString() {
      return remoteAddress;
    }
  }
}
