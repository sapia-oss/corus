package org.sapia.corus.cloud.platform.rest.helper;

import java.io.IOException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.sapia.corus.cloud.platform.rest.CorusRestClient;
import org.sapia.corus.cloud.platform.rest.FeedbackHandler;
import org.sapia.corus.cloud.platform.rest.JSONValue;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.util.TimeSupplier;

import net.sf.json.JSONArray;

/**
 * Utility class used to process aynchronous progress responses received from Corus.
 * 
 * 
 * @author yduchesne
 *
 */
public class AsyncResponseHelper {

  private FeedbackHandler handler      = new FeedbackHandler.NullFeedbackHandler() ;
  private TimeSupplier    timeSupplier = TimeSupplier.SystemTime.getInstance();
  private CorusRestClient client;
  private TimeMeasure     pollInterval;
  private String          completionToken;
 
  public AsyncResponseHelper(CorusRestClient client, TimeMeasure pollInterval, String completionToken) {
    this.client          = client;
    this.pollInterval    = pollInterval;
    this.completionToken = completionToken;
  }
  
  public AsyncResponseHelper withFeedbackHandler(FeedbackHandler handler) {
    this.handler = handler;
    return this;
  }
  
  public AsyncResponseHelper withTimeSupplier(TimeSupplier timeSupplier) {
    this.timeSupplier = timeSupplier;
    return this;
  }
   
  public int complete() throws IOException, InterruptedException {
    
    WebTarget resource = client.resource("/progress/" + completionToken);
    
    int status = CorusRestClient.STATUS_IN_PROGRESS;
        
    do {
      JSONValue jsonResponse = resource.request()
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .get(JSONValue.class);
      
      status = jsonResponse.asObject().getInt("status");
      String[] messages = new String[0];
      if (jsonResponse.asObject().containsKey("feedback")) {
        JSONArray feedback = jsonResponse.asObject().getJSONArray("feedback");
        messages = new String[feedback.size()];
        for (int i = 0; i < feedback.size(); i++) {
          messages[i] = feedback.getString(i);
        }
      }
      
      switch (status) {
        case CorusRestClient.STATUS_SERVER_ERROR:
          handler.onErrorFeedback(messages);
          break;
        default:
          handler.onInfoFeedback(messages);
      }
      
    } while (timeSupplier.sleepConditionally(pollInterval, status == CorusRestClient.STATUS_ERROR_IN_PROGRESS || status == CorusRestClient.STATUS_IN_PROGRESS));

    return status;
  }
}
