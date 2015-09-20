package org.sapia.corus.cloud.aws.image.creation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.sapia.corus.cloud.aws.util.RetryLatch;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.google.common.io.CharStreams;

/**
 * Waits for the completion of Corus' installation (invokes the Corus <code>/ping</code> endpoint for 
 * the check).
 * 
 * @author yduchesne
 *
 */
public class WaitForCompletedCorusInstall implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "Waiting for completion of Corus installation";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    String urlString = "http://" + context.getAllocatedPublicIp() + ":" + context.getConf().getCorusPort() + "/ping";
    context.getLog().info("Connecting to Corus ping URL at: %s", urlString);
    URL corusUrl = new URL(urlString);
    RetryLatch latch = new RetryLatch(context.getConf().getCorusIntallCheckRetry());
    do {
      try {
        String response = getCorusResponse(context, corusUrl);
        if (response.toLowerCase().contains("cloud_ready")) {
          context.getLog().info("Received Corus installation confirmation");
          break;
        }
      } catch (IOException e) {
        context.getLog().warning("I/O error caught while trying to connect to Corus: %s. Installation might not be finished, will retry", 
            e.getMessage());
      } 
    } while (latch.incrementAndPause().shouldContinue());
  }
  
  // Meant for overridding in the context of unit testing
  protected String getCorusResponse(ImageCreationContext context, URL corusUrl) throws IOException, IllegalStateException {
    HttpURLConnection corusConn = (HttpURLConnection) corusUrl.openConnection();
    corusConn.setDoOutput(false);
    corusConn.setDoInput(true);
    corusConn.setRequestMethod("GET");
    InputStream is = corusConn.getInputStream();
    try {
      String response = CharStreams.toString(new InputStreamReader(is));
      context.getLog().info("Got response from Corus: %s", response);
      if (corusConn.getResponseCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Could not get installation status from Corus. HTTP response status: " + corusConn.getResponseCode());
      }
      return response;
    } finally {
      is.close();
      corusConn.disconnect();
    }    
  }
  
}
