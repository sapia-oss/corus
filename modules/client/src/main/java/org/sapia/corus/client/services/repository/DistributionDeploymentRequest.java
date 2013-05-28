package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;

/**
 * Sent to a Corus repository node so that deployment of the distributions that are specified
 * by an instance of this class is triggered.
 * 
 * @author yduchesne
 *
 */
public class DistributionDeploymentRequest extends ArtifactDeploymentRequest {
  
  static final long serialVersionID = 1L;
  
  public static final String EVENT_TYPE = "corus.event.repository.request.deployment";
  
  private Endpoint           endpoint;
  private List<RepoDistribution> distributions = new ArrayList<RepoDistribution>();

  /**
   * Do not use: meant for externalization.
   */
  public DistributionDeploymentRequest() {
    super();
  }
  
  /**
   * @param requesterAddr the {@link Endpoint} of the requester from which
   * this instance originates.
   */
  public DistributionDeploymentRequest(Endpoint endpoint) {
    super(endpoint);
  }
  
  /**
   * @return this instance's  unmodifiable {@link List} of {@link RepoDistribution}.
   */
  public List<RepoDistribution> getDistributions() {
    return Collections.unmodifiableList(distributions);
  }
  
  /**
   * @param dist a {@link RepoDistribution} to add to this instance.s
   */
  public void addDistribution(RepoDistribution dist) {
    distributions.add(dist);
  }

  /**
   * @param dist a {@link RepoDistribution} to add to this instance.s
   */
  public void addDistributions(Collection<RepoDistribution> dist) {
    distributions.addAll(dist);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
    this.endpoint = (Endpoint) in.readObject();
    this.distributions = (List<RepoDistribution>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(endpoint);
    out.writeObject(distributions);
  }

  
}
