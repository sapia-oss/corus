package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * This response is sent by a Corus server that acts as a repository: it holds
 * the list of {@link RepoDistribution} instances corresponding to the
 * distributions that it holds.
 * 
 * @author yduchesne
 * 
 */
public class DistributionListResponse implements Externalizable {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.response.distributions";

  private Endpoint endpoint;
  private List<RepoDistribution> distributions = new ArrayList<RepoDistribution>();

  /**
   * Do not call: meant for externalization
   */
  public DistributionListResponse() {
  }

  /**
   * @param endpoint
   *          the {@link Endpoint} of the Corus node from which this response
   *          originates.
   */
  public DistributionListResponse(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * @return the {@link Endpoint} of the node from which this instance
   *         originates.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * @return this instance's unmodifiable {@link List} of
   *         {@link RepoDistribution}.
   */
  public List<RepoDistribution> getDistributions() {
    return Collections.unmodifiableList(distributions);
  }

  /**
   * @param dist
   *          a {@link RepoDistribution} to add to this instance.s
   */
  public void addDistribution(RepoDistribution dist) {
    distributions.add(dist);
  }

  /**
   * @param dist
   *          a {@link RepoDistribution} to add to this instance.s
   */
  public void addDistributions(Collection<RepoDistribution> dist) {
    distributions.addAll(dist);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.endpoint = (Endpoint) in.readObject();
    this.distributions = (List<RepoDistribution>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
    out.writeObject(distributions);
  }

}
