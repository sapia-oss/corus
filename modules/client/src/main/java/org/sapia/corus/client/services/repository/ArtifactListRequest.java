package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * This request is sent to a Corus server that acts as a repository in order to
 * obtain the list of distributions that it currently holds.
 * 
 * @author yduchesne
 * 
 */
public class ArtifactListRequest implements Externalizable {

  static final long serialVersionID = 1L;

  public static final String EVENT_TYPE = "corus.event.repository.request.artifacts";

  private Endpoint endpoint;

  /**
   * Do not use: meant for externalization.
   */
  public ArtifactListRequest() {
  }

  /**
   * @param endpoint
   *          the {@link Endpoint} of the requester from which this instance
   *          originates.
   */
  public ArtifactListRequest(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.endpoint = (Endpoint) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
  }

}
