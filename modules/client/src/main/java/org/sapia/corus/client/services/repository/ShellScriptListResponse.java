package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;

/**
 * This response is sent by a Corus server that acts as a repository: it holds the list
 * of {@link ShellScript} instances corresponding to the distributions that it holds.
 * 
 * @author yduchesne
 *
 */
public class ShellScriptListResponse implements Externalizable {

  static final long serialVersionID = 1L;
  
  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.response.scripts";
 
  private Endpoint          endpoint;
  private List<ShellScript> scripts;

  /**
   * Do not call: meant for externalization only.
   */
  public ShellScriptListResponse() {
  }
  
  /**
   * @param endpoint the {@link Endpoint} of the node from which this instance originates.
   * @param scripts this instance's unmodifiable {@link List} of {@link ShellScript}s.
   */
  public ShellScriptListResponse(Endpoint endpoint, List<ShellScript> scripts) {
    this.endpoint = endpoint;
    this.scripts  = scripts;
  }
  
  /**
   * @return the {@link Endpoint} of the node from which this instance originates.
   */
  public Endpoint getEndpoint() {
    return endpoint;
  }
  
  /**
   * @return this instance's unmodifiable {@link List} of {@link ShellScript}s.
   */
  public List<ShellScript> getScripts() {
    return scripts;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    endpoint = (Endpoint) in.readObject();
    scripts = (List<ShellScript>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(endpoint);
    out.writeObject(scripts);
  }

}
