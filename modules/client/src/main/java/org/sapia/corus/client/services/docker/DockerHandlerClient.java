package org.sapia.corus.client.services.docker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;
import org.simpleframework.http.Status;

/**
 * Provides Docker-specific client functionality.
 * 
 * @author yduchesne
 */
public class DockerHandlerClient {

  public static final String DOCKER_CONTEXT = "/corus/docker";
  
  private static final int BUFSZ = 8092;

  private HttpURLConnection conn;
  private URL               url;
  
  public DockerHandlerClient(ServerAddress addr) throws DockerClientException {
    try {
      HttpAddress targetAddress = (HttpAddress) addr;
      url = new URL(Uri.parse("http://" + targetAddress.getHost() + ":" + targetAddress.getPort() + DOCKER_CONTEXT).toString());
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Instance of " + HttpAddress.class.getName() + " expected");
    } catch (IOException e) {
      throw new DockerClientException("Could create connection URL", e);
    }
  }
  
  public InputStream getImage(String imageName, AuditInfo auditInfo) throws DockerClientException {
    Assertions.isFalse(Strings.isBlank(imageName), "Docker image name must be specified");
    Assertions.illegalState(!auditInfo.isEncrypted(), "Expected encrypted AuditInfo");
    HttpURLConnection connection = connection();
    try {
      ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(connection.getOutputStream());
      oos.writeObject(new DockerSaveRequest(auditInfo, imageName));
      oos.flush();
      oos.close();
      if (connection.getResponseCode() == Status.OK.code) {
        return connection.getInputStream();
      } else {
        throw new DockerClientException(
            String.format(
                "Could not get Docker image (status code: %s): %s", 
                  connection.getResponseCode(), connection.getResponseMessage()
              )
        );
 
      } 
    } catch (IOException e) {
      throw new DockerClientException("Error occurred while fetching Docker image", e);
    }
  }
  
  public void saveImage(String imageName, String imagePath, AuditInfo auditInfo) throws DockerClientException {
    Assertions.isFalse(Strings.isBlank(imageName), "Docker image name must be specified");
    Assertions.illegalState(!auditInfo.isEncrypted(), "Expected encrypted AuditInfo");
    try (FileOutputStream imageOutput = new FileOutputStream(imagePath)) {
      try (InputStream imageInput = getImage(imageName, auditInfo)) {
        IOUtil.transfer(imageInput, imageOutput, BUFSZ);       
      }
    } catch (IOException e) {
      throw new DockerClientException("Error occurred while saving Docker image", e);
    }
  }
  
  public void close() {
    if (conn != null) {
      conn.disconnect();
      conn = null;
    }
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private HttpURLConnection connection() throws DockerClientException {
    if (conn == null) {
      try {
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
      } catch (IOException e) {
        throw new DockerClientException("Error occurred while trying to create HTTP connection", e);
      }
    }
    return conn;
  }

}
