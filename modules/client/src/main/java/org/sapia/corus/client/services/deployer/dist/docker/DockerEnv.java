package org.sapia.corus.client.services.deployer.dist.docker;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.services.deployer.dist.Property;

/**
 * Allows defining Docker environment properties.
 * 
 * @author yduchesne
 *
 */
public class DockerEnv {
  
  private List<Property> properties = new ArrayList<Property>();
  
  public Property createProperty() {
    Property p = new Property();
    properties.add(p);
    return p;
  }
  
  public List<Property> getProperties() {
    return properties;
  }

}
