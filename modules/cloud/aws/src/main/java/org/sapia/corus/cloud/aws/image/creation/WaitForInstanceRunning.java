package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.util.RetryCriteria;

/**
 * Extends the {@link InstanceStatusCheckSupport} to wait for a just lunched instance
 * reaching the <code>RUNNING</code> status.
 * 
 * @author yduchesne
 *
 */
public class WaitForInstanceRunning extends InstanceStatusCheckSupport {
  
  public WaitForInstanceRunning() {
    super("pausing until instance is running", InstanceStatusCode.RUNNING);
  }
  
  @Override
  protected RetryCriteria doGetRetryCriteria(ImageCreationContext context) {
    return context.getSettings().getNotNull("instanceRunCheckRetry").get(RetryCriteria.class);
  }

}
