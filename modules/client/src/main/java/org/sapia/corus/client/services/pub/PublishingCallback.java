package org.sapia.corus.client.services.pub;

import org.sapia.corus.client.services.processor.Process;

/**
 * Instances of this interface are notified at different stages during publishing.
 * 
 * @author yduchesne
 *
 */
public interface PublishingCallback {
  
  public void publishingStarted(ProcessPubContext ctx);
  
  public void publishingSuccessful(ProcessPubContext ctx);
  
  public void publishingFailed(ProcessPubContext ctx, Exception err);
  
  public void publishingNotApplicable(Process p);
  
}