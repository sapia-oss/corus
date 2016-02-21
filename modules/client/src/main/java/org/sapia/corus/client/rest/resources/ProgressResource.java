package org.sapia.corus.client.rest.resources;

import java.util.concurrent.TimeoutException;

import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.rest.Accepts;
import org.sapia.corus.client.rest.ContentTypes;
import org.sapia.corus.client.rest.DefaultContentLevel;
import org.sapia.corus.client.rest.HttpMethod;
import org.sapia.corus.client.rest.Output;
import org.sapia.corus.client.rest.Path;
import org.sapia.corus.client.rest.RequestContext;
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
