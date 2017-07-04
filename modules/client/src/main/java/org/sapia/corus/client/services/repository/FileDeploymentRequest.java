package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;

/**
 * Sent to a Corus repository node so that deployment of the files that are
 * specified by an instance of this class is triggered.
 * 
 * @author yduchesne
 * 
 */
public class FileDeploymentRequest extends ArtifactDeploymentRequest {

  static final long serialVersionID = 1L;

  public static final String EVENT_TYPE = "corus.event.repository.request.files";

  private Endpoint       endpoint;
  private List<FileInfo> files     = new ArrayList<FileInfo>();
  
  /**
   * Do not use: meant for externalization.
   */
  public FileDeploymentRequest() {
  }

  /**
   * @param endpoint
   *          the {@link Endpoint} of the requester from which this instance
   *          originates.
   * @param files
   *          the {@link FileInfo} instances corresponding to the files that are
   *          requested.
   */
  public FileDeploymentRequest(Endpoint endpoint, Collection<FileInfo> files) {
    super(endpoint);
    this.files.addAll(files);
  }

  /**
   * @return this instance's unmodifiable {@link List} of {@link FileInfo}
   *         instances.
   */
  public List<FileInfo> getFiles() {
    return Collections.unmodifiableList(files);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    endpoint = (Endpoint) in.readObject();
    files    = (List<FileInfo>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(endpoint);
    out.writeObject(files);
  }

}
