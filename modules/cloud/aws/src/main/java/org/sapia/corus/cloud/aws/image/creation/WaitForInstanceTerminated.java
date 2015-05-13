package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.platform.util.RetryCriteria;

/**
 * Extends the {@link InstanceStatusCheckSupport} to wait for a just lunched instance
 * reaching the <code>TERMINATED</code> status.
 * 
 * @author yduchesne
 *
 */
public class WaitForInstanceTerminated extends InstanceStatusCheckSupport {
  
  public WaitForInstanceTerminated() {
    super("pausing until instance has terminated", InstanceStatusCode.TERMINATED);
  }
  
  @Override
  protected RetryCriteria doGetRetryCriteria(ImageCreationContext context) {
    return context.getConf().getInstanceTerminatedCheckRetry();
  }

}
