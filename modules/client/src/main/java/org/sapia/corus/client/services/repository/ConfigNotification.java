package org.sapia.corus.client.services.repository;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.configurator.Property;

/**
 * Holds properties and tags to send to targeted nodes.
 * 
 * @author yduchesne
 * 
 */
public class ConfigNotification extends ClusterNotification {

  static final long serialVersionID = 1L;

  /**
   * The remote event type corresponding to an instance of this class.
   */
  public static final String EVENT_TYPE = "corus.event.repository.notif.config";

  private List<Property> properties = new ArrayList<>();
  private Set<String>    tags       = new HashSet<>();

  public ConfigNotification() {
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  /**
   * @param props
   *          a {@link List} of {@link Property} instances.
   */
  public void addProperties(List<Property> props) {
    properties.addAll(props);
  }

  /**
   * @return this instance's {@link Property} instances.
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * @param tags
   *          the {@link Set} of tags to add to this instance.
   */
  public void addTags(Set<String> tags) {
    this.tags.addAll(tags);
  }

  /**
   * @return this instance's tags.
   */
  public Set<String> getTags() {
    return tags;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    properties = (List<Property>) in.readObject();
    tags = (Set<String>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(properties);
    out.writeObject(tags);
  }
}
