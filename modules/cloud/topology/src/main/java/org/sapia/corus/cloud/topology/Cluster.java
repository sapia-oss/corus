package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>cluster</code> element.
 * 
 * @author yduchesne
 *
 */
public class Cluster extends ClusterTemplate implements TopologyElement, XmlStreamable, Validateable {
  
  private String templateRef;
  
  public void setTemplateRef(String templateRef) {
    this.templateRef = templateRef;
  }
  
  public String getTemplateRef() {
    return templateRef;
  }
  
  @Override
  public void render(TopologyContext context) {
    if (templateRef != null) {
      ClusterTemplate template = context.resolveClusterTemplate(templateRef);
      copyFrom(template);
      addParams(template.getParams());
    }
  }
  
  public Set<Machine> getRepoServerMachines() {
    Set<Machine> toReturn = new HashSet<Machine>();
    for (Machine m : getMachines()) {
      if (m.getRepoRole().equals(Machine.REPO_ROLE_SERVER)) {
        if (m.getMinInstances() == 0) {
          m.setMinInstances(1);
        }
        toReturn.add(m);
      }
    }
    if (toReturn.isEmpty()) {
      // no repo server set: setting the first occurring machine as the repo server
      Machine selected = getMachines().iterator().next();
      selected.setRepoRole(Machine.REPO_ROLE_SERVER);
      selected.setMinInstances(1);
      toReturn.add(selected);
    }
    return toReturn;
  }
  
  public Set<Machine> getRepoClientMachines() {
    Set<Machine> toReturn = new HashSet<Machine>();
    for (Machine m : getMachines()) {
      if (m.getRepoRole().equals(Machine.REPO_ROLE_CLIENT)) {
        toReturn.add(m);
      }
    }
    return toReturn;
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (getName() == null) {
      throw new IllegalArgumentException("'name' attribute not set on <cluster>");
    }
    if (getInstances() <= 0) {
      throw new IllegalArgumentException("Invalid value for 'instances' attribute of <cluster> element: " + getName() + ". Must be > 0");
    }
    if (getMachines().isEmpty()) {
      throw new IllegalArgumentException("<cluster> element " + getName() + " does not have any <machine> child element");
    }
    
    for (Machine m  : getMachines()) {
      m.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreamable

  @Override
  public void output(XmlStream stream) {
    stream.beginElement("cluster");
    stream.attribute("name", getName());
    stream.attribute("instances", getInstances());
    for (Machine m : getMachines()) {
      m.output(stream);
    }
    stream.endElement("cluster");
  }
}
