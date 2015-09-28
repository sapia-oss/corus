package org.sapia.corus.publisher;

import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;

/**
 * Specifies the behavior for publishing processes for remote discovery, and unpublishing them upon shutdown.
 * 
 * @author yduchesne
 *
 */
public interface ProcessPublishingProvider {
  
  /**
   * @param config the {@link ProcessPubConfig} corresponding to a publishing operation to perform.
   * @return <code>true</code> if this instance can handle the given config.
   */
  boolean accepts(ProcessPubConfig config);
  
  /**
   * @param context a {@link ProcessPubContext} instance holding the process-related data pertaining to this operation.
   * @param callback a {@link PublishingCallback} to notify in the context of this operation.
   */
  void publish(ProcessPubContext context, PublishingCallback callback);
  
  /**
   * @param context a {@link ProcessPubContext} instance holding the process-related data pertaining to this operation.
   * @param callback an {@link UnpublishingCallback} to notify in the context of this operation.
   */
  void unpublish(ProcessPubContext context, UnpublishingCallback callback);

}
