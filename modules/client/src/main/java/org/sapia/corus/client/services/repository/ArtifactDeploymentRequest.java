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
  private boolean  force;

  /**
   * Do not call: meant for externalization.
   */
  protected ArtifactDeploymentRequest() {

  }

  /**
   * @param endpoint
   *          the {@link Endpoint} corresponding to the Corus node from which
   *          this request originates.
   */
  protected ArtifactDeploymentRequest(Endpoint endpoint) {
    this.endpoint = endpoint;
  }
  
  /**
   * Sets this instance's <code>force</code>.
   * 
   */
  public ArtifactDeploymentRequest setForce(boolean force) {
    this.force = force;
    return this;
  }
 
  /**
   * @return <code>true</code> if the deployment should be performed whether the 
   * node receiving this request is a repo node or not, and regardless if it has
   * its corresponding "push" flag turn off.
   */
  public boolean isForce() {
    return force;
  }

  /**
   * @return the {@link Endpoint} corresponding to the Corus node from which
   *         this request originates.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }

  // --------------------------------------------------------------------------
  // Externalizable interface

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    endpoint = (Endpoint) in.readObject();
    force    = in.readBoolean();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
    out.writeBoolean(force);
  }

}
