package org.sapia.corus.client.services.repository;

import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * Sent by a client when it wants only the configuration (no files or distributions).
 * 
 * @author yduchesne
 *
 */
public class ConfigDeploymentRequest extends ArtifactDeploymentRequest {

	static final long serialVersionID = 1L;

	public static final String EVENT_TYPE = "corus.event.repository.request.config";
	
	public ConfigDeploymentRequest() {
  }
	
  /**
   * @param endpoint
   *          the {@link Endpoint} of the requester from which this instance
   *          originates.
   */
	public ConfigDeploymentRequest(Endpoint endpoint) {
	  super(endpoint);
	}
}
