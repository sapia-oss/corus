package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.ubik.util.Strings;

/**
 * Holds {@link ExecConfig} instances.
 * 
 * @author yduchesne
 *
 */
public class ExecConfigNotification extends ClusterNotification {

  static final long serialVersionID = 1L;
  
  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.exec-config";
 
  private List<ExecConfig> configs = new ArrayList<ExecConfig>();

  /** Do not use: meant for serialization only */
  public ExecConfigNotification() {
  }
  
  /**
   * @param configs a {@link List} of {@link ExecConfig} instances.
   */
  public ExecConfigNotification(List<ExecConfig> configs) {
    this.configs      = configs;
  }
  
  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }
  
  /**
   * @return this instance's {@link List} of {@link ExecConfig}s.
   */
  public List<ExecConfig> getConfigs() {
    return configs;
  }

  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    super.readExternal(in);
    this.configs = (List<ExecConfig>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(configs);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, 
        "configs", configs, 
        "targeted", getTargets(), 
        "visited", getVisited());
  }

}
