package org.sapia.corus.client.services.pub;

import org.sapia.corus.client.services.processor.Process;

public interface ProcessPublisher {
  
  public static final String ROLE = ProcessPublisher.class.getName();
  
  public void publishProcess(Process process, PublishingCallback callback);
  
  public void unpublishProcess(Process process, UnpublishingCallback callback);

}
