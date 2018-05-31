package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;

/**
 * This response is sent by a Corus server that acts as a repository: it holds
 * the list of {@link FileInfo} instances corresponding to the files that it
 * holds.
 * 
 * @see FileManager
 * 
 * @author yduchesne
 * 
 */
public class FileListResponse implements Externalizable {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.response.files";

  private Endpoint       endpoint;
  private List<FileInfo> files;
  private boolean        force;

  /**
   * Do not call: meant for externalization only.
   */
  public FileListResponse() {
  }

  /**
   * @param endpoint
   *          the {@link Endpoint} of the node from which this instance
   *          originates.
   * @param scripts
   *          this instance's unmodifiable {@link List} of {@link FileInfo}s.
   */
  public FileListResponse(Endpoint endpoint, List<FileInfo> files) {
    this.endpoint = endpoint;
    this.files = files;
  }

  /**
   * @return the {@link Endpoint} of the node from which this instance
   *         originates.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }

  /**
   * @return this instance's unmodifiable {@link List} of {@link FileInfo}s.
   */
  public List<FileInfo> getFiles() {
    return files;
  }
  
  /**
   * Sets this instance's <code>force</code>.
   * 
   */
  public FileListResponse setForce(boolean force) {
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

  // --------------------------------------------------------------------------
  // Externalizable

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    endpoint = (Endpoint) in.readObject();
    files    = (List<FileInfo>) in.readObject();
    force    = in.readBoolean();    
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
    out.writeObject(files);
    out.writeBoolean(force);
  }

}
