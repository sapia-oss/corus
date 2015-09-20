package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.aws.util.RetryCriteria;

/**
 * Extends the {@link InstanceStatusCheckSupport} to wait for a just lunched instance
 * reaching the <code>RUNNING</code> status.
 * 
 * @author yduchesne
 *
 */
public class WaitForInstanceRunning extends InstanceStatusCheckSupport {
  
  public WaitForInstanceRunning() {
    super("Pausing until instance is running", InstanceStatusCode.RUNNING);
  }
  
  @Override
  protected RetryCriteria doGetRetryCriteria(ImageCreationContext context) {
    return context.getConf().getInstanceRunCheckRetry();
  }

}
