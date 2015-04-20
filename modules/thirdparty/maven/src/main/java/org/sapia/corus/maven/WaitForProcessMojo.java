package org.sapia.corus.maven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal that tries to connect to a given url and fails after the number
 * of attempts is greater than the allowed maxRetry. This plugin is useful to
 * pause the build while a server is being started before running integration
 * test.
 */
@Mojo(name = "wait", defaultPhase = LifecyclePhase.PACKAGE)
public class WaitForProcessMojo extends BaseCorusMojoSupport {

  /**
   * The URL to test for completion.
   */
  @Parameter(required = true, property = "url")
  private String urlString;

  /**
   * The timeout period in milliseconds to wait between attempts.
   */
  @Parameter(property = "timeout", defaultValue = "30000")
  private int timeoutMillis;

  /**
   * The maximum number of attempt to connect.
   */
  @Parameter(property = "maxRetry", defaultValue = "1")
  private int maxRetry;

  public void setUrl(String url) {
    this.urlString = url;
  }

  public void setTimeout(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  public void setMaxRetry(int maxRetry) {
    this.maxRetry = maxRetry;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.maven.CorusMojoSupport#doExecute()
   */
  @Override
  protected void doExecute() throws MojoExecutionException, MojoFailureException {
    int count = maxRetry;
    int attempt = 1;

    getLog().info("Waiting for process using parameters: url=" + urlString + " timeoutMillis=" + timeoutMillis + " maxRetry=" + maxRetry);

    while (true) {
      long attemptStartTimeMillis = System.currentTimeMillis();

      try {
        getLog().info("Connecting to process [Attempt #" + attempt +  "] ...");

        URL testUrl = new URL(urlString);
        boolean result;
        if ("http".equalsIgnoreCase(testUrl.getProtocol())) {
          result = performHttpCheck(testUrl, timeoutMillis/2, timeoutMillis/2);
        } else {
          String message = "The specified 'url' protocol is not suported : " + urlString;
          getLog().error(message);
          throw new MojoExecutionException(message);
        }

        if (result) {
          getLog().info("success - reached " + urlString);
          break;
        } else {
          throw new RuntimeException("Unable to connect yet...");
        }

      } catch (IOException | RuntimeException e) {
        if (count > 1) {
          count--;
        } else if (count != 0) {
          String message = "Cannot connect --> " + e.getMessage();
          getLog().error(message);
          throw new MojoExecutionException(message, e);
        }

        long sleepTimeMillis = timeoutMillis - (System.currentTimeMillis() - attemptStartTimeMillis);
        if (sleepTimeMillis > 0) {
          getLog().info("Sleeping until next attempt...  (" + sleepTimeMillis + "ms)");
          try {
            Thread.sleep(sleepTimeMillis);
          } catch (InterruptedException e1) { // do nothing
          }
        }

        attempt++;

      } catch (Exception e) {
        String message = "System error performing connection test: " + e.getMessage();
        getLog().error(message);
        throw new MojoFailureException(message, e);
      }
    }
  }

  /**
   * Internal method that validates the state of a process using an http get request.
   *
   * @param httpUrl The URL of the http resource to obtain.
   * @param connecTimeoutMillis The socket connection timeout in milliseconds.
   * @param readTimeOutMillis The read connection timeout in milliseconds.
   * @return True if the http get request returns an http code 200 (HTTP OK).
   * @throws IOException If an connectivity erro occurs durinh the verification.
   */
  protected boolean performHttpCheck(URL httpUrl, int connecTimeoutMillis, int readTimeOutMillis) throws IOException {
    // Configure connection
    HttpURLConnection httpConn = (HttpURLConnection) httpUrl.openConnection();
    httpConn.setRequestMethod("GET");
    httpConn.setDoOutput(true);
    httpConn.setUseCaches(false);
    httpConn.setInstanceFollowRedirects(true);
    httpConn.setConnectTimeout(connecTimeoutMillis);
    httpConn.setReadTimeout(readTimeOutMillis);

    // Peforms http get request
    getLog().info("Peforming http get request to " + httpUrl.toString() + " ...");
    long connectTimeMillis = System.currentTimeMillis();
    httpConn.connect();

    // Read the output from the server
    BufferedReader responseReader = null;
    int responseCode = -1;
    try {
      responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
      StringBuilder stringBuilder = new StringBuilder();
      Readers.transfertTo(responseReader, stringBuilder);
      responseCode = httpConn.getResponseCode();

      getLog().info("Got server response code " + responseCode + " " + httpConn.getResponseMessage() +
            " in " + (System.currentTimeMillis() - connectTimeMillis) + "ms");
      getLog().debug("Server response content\n" + stringBuilder.toString());

    } finally {
      if (responseReader != null) {
        try {
          responseReader.close();
        } catch (IOException e) {
        }
      }
      httpConn.disconnect();
    }

    return (HttpURLConnection.HTTP_OK == responseCode);
  }

}
