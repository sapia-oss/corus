package org.sapia.corus.client.services.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient;
import org.sapia.corus.client.services.deployer.transport.DeploymentClient;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Streams;

/**
 * Implements the {@link DeploymentClient} interface over HTTP.
 * 
 * @author Yanick Duchesne
 */
public class HttpDeploymentClient extends AbstractDeploymentClient {

  public static final int CHUNKED_CONTENT_LEN = 8000;
  public static final int HTTP_CONNECTION_TIMEOUT_MILLIS = 3000;
  public static final int HTTP_READ_TIMEOUT_MILLIS = 60000;
  
  public static final String DEPLOYER_CONTEXT = "/corus/deployer";

  private URL url;
  private HttpURLConnection conn;
  private InputStream input;
  private OutputStream output;

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#close()
   */
  public void close() {
    if (conn != null) {
      Streams.closeSilently(output);
      Streams.closeSilently(input);
      conn.disconnect();
    }
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#connect(org.sapia.ubik.net.ServerAddress)
   */
  public void connect(ServerAddress addr) throws IOException {
    try {
      HttpAddress targetAddress = (HttpAddress) addr;
      url = new URL(Uri.parse("http://" + targetAddress.getHost() + ":" + targetAddress.getPort() + DEPLOYER_CONTEXT).toString());
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Instance of " + HttpAddress.class.getName() + " expected");
    }
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (input == null) {
      input = connection().getInputStream();
    }
    return input;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (output == null) {
      output = connection().getOutputStream();
    }
    return output;
  }

  private HttpURLConnection connection() throws IOException {
    if (conn == null) {
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setUseCaches(false);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setConnectTimeout(HTTP_CONNECTION_TIMEOUT_MILLIS);
      conn.setReadTimeout(HTTP_READ_TIMEOUT_MILLIS);
      conn.setChunkedStreamingMode(CHUNKED_CONTENT_LEN);
    }
    return conn;
  }
}
