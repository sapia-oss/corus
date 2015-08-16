package org.sapia.corus.client.rest;

import java.util.concurrent.TimeoutException;

import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.async.ProgressCapableTask;

/**
 * Allows fetching {@link ProgressResult}s asynchronously, for given {@link ProgressCapableTask}s.
 * 
 * @author yduchesne
 *
 */
public class ProgressResource {
  
  @Path({
    "/progress/{corus:completionToken}"
  })
  @HttpMethod(HttpMethod.GET)
  @Output(ContentTypes.APPLICATION_JSON)
  @Accepts({ContentTypes.APPLICATION_JSON, ContentTypes.ANY})
  @DefaultContentLevel(ContentLevel.SUMMARY)
  public ProgressResult getProgressForToken(RequestContext context) throws TimeoutException {
    Value        completionToken  = context.getRequest().getValue("corus:completionToken");
    ProgressCapableTask task = context.getAsyncService().getAsyncTask(completionToken.asString(), ProgressCapableTask.class);
    return task.getNextResult();
  }  
}
