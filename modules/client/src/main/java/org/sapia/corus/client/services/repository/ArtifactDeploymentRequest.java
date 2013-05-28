package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * Base class for artifact deployment requests.
 * 
 * @author yduchesne
 *
 */
public abstract class ArtifactDeploymentRequest implements Externalizable {
  
  static final long serialVersionID = 1L;
  
  private Endpoint endpoint;
  
  /**
   * Do not call: meant for externalization.
   */
  protected ArtifactDeploymentRequest() {
   
  }
  
  /**
   * @param endpoint the {@link Endpoint} corresponding to the Corus node from which this request
   * originates.
   */
  protected ArtifactDeploymentRequest(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
  
  /**
   * @return the {@link Endpoint} corresponding to the Corus node from which this request
   * originates.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    endpoint = (Endpoint) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
  }

}
