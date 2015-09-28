package org.sapia.corus.client.services.pub;

import org.sapia.corus.client.services.processor.Process;

/**
 * Instances of this interface are notified at different stages during unpublishing.
 * 
 * @author yduchesne
 *
 */
public interface UnpublishingCallback {
  
  public void unpublishingStarted(ProcessPubContext ctx);
  
  public void unpublishingSuccessful(ProcessPubContext ctx);
  
  public void unpublishingFailed(ProcessPubContext ctx, Exception err);
  
  public void unpublishingNotApplicable(Process p);
  
}