package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.util.RetryCriteria;

/**
 * Extends the {@link InstanceStatusCheckSupport} to wait for a just lunched instance
 * reaching the <code>STOPPED</code> status.
 * 
 * @author yduchesne
 *
 */
public class WaitForInstanceStopped extends InstanceStatusCheckSupport {
  
  public WaitForInstanceStopped() {
    super("pausing until instance has stopped", InstanceStatusCode.STOPPED);
  }
  
  @Override
  protected RetryCriteria doGetRetryCriteria(ImageCreationContext context) {
    return context.getSettings().getNotNull("instanceStopCheckRetry").get(RetryCriteria.class);
  }

}
